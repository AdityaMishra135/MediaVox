package org.skywaves.mediavox.activities

import OnSwipeTouchListener
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Video
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import org.skywaves.mediavox.BuildConfig
import org.skywaves.mediavox.R
import org.skywaves.mediavox.adapters.DirectoryAdapter
import org.skywaves.mediavox.core.dialogs.CreateNewFolderDialog
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.dialogs.RadioGroupDialog
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.core.helpers.*
import org.skywaves.mediavox.core.models.FileDirItem
import org.skywaves.mediavox.core.models.RadioItem
import org.skywaves.mediavox.core.models.Release
import org.skywaves.mediavox.core.views.MyGridLayoutManager
import org.skywaves.mediavox.core.views.MyRecyclerView
import org.skywaves.mediavox.core.views.customTriStateSwitch.RMTristateSwitch.STATE_LEFT
import org.skywaves.mediavox.core.views.customTriStateSwitch.RMTristateSwitch.STATE_MIDDLE
import org.skywaves.mediavox.core.views.customTriStateSwitch.RMTristateSwitch.STATE_RIGHT
import org.skywaves.mediavox.databases.GalleryDatabase
import org.skywaves.mediavox.databinding.ActivityMainBinding
import org.skywaves.mediavox.dialogs.ChangeSortingDialog
import org.skywaves.mediavox.dialogs.ChangeViewTypeDialog
import org.skywaves.mediavox.dialogs.FilterMediaDialog
import org.skywaves.mediavox.dialogs.GrantAllFilesDialog
import org.skywaves.mediavox.extensions.*
import org.skywaves.mediavox.fragments.main.ToolsFragment
import org.skywaves.mediavox.fragments.settings.SettingsThemeFragment
import org.skywaves.mediavox.helpers.*
import org.skywaves.mediavox.interfaces.DirectoryOperationsListener
import org.skywaves.mediavox.jobs.NewPhotoFetcher
import org.skywaves.mediavox.models.Directory
import org.skywaves.mediavox.models.Medium
import java.io.*


class MainActivity : SimpleActivity(), DirectoryOperationsListener {
    companion object {
        private const val PICK_MEDIA = 2
        private const val LAST_MEDIA_CHECK_PERIOD = 3000L
    }

    private var mIsPickVideoIntent = false
    private var mIsPickAudioIntent = false
    private var mIsGetVideoContentIntent = false
    private var mIsGetAudioContentIntent = false
    private var mIsGetAnyContentIntent = false
    private var mAllowPickingMultiple = false
    private var mIsThirdPartyIntent = false
    private var mIsGettingDirs = false
    private var mLoadedInitialPhotos = false
    private var mIsPasswordProtectionPending = false
    private var mWasProtectionHandled = false
    private var mShouldStopFetching = false
    private var mWasDefaultFolderChecked = false
    private var mWasMediaManagementPromptShown = false
    private var mWasUpgradedFromFreeShown = false
    private var mLatestMediaId = 0L
    private var mLatestMediaDateId = 0L
    private var mCurrentPathPrefix =
        ""                 // used at "Group direct subfolders" for navigation
    private var mOpenedSubfolders =
        arrayListOf("")     // used at "Group direct subfolders" for navigating Up with the back button
    private var mDateFormat = ""
    private var mTimeFormat = ""
    private var mLastMediaHandler = Handler()
    private var mTempShowHiddenHandler = Handler()
    private var mLastMediaFetcher: MediaFetcher? = null
    private var mDirs = ArrayList<Directory>()
    private var mDirsIgnoringSearch = ArrayList<Directory>()

    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mStoredStyleString = ""
    private var mStoredLastPlayed = ""
    var mFragmentManager: FragmentManager? = null
    var toolShow = false
    val binding by viewBinding(ActivityMainBinding::inflate)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)

        if (savedInstanceState == null) {
            config.temporarilyShowHidden = false
            config.temporarilyShowExcluded = false
            config.tempSkipDeleteConfirmation = false
            config.tempSkipRecycleBin = false
            removeTempFolder()
            checkRecycleBinItems()
            startNewPhotoFetcher()
        }
        mFragmentManager = supportFragmentManager

        mIsPickVideoIntent = isPickVideoIntent(intent)
        mIsPickAudioIntent = isPickAudioIntent(intent)
        mIsGetVideoContentIntent = isGetVideoContentIntent(intent)
        mIsGetAudioContentIntent = isGetAudioContentIntent(intent)
        mIsGetAnyContentIntent = isGetAnyContentIntent(intent)
        mAllowPickingMultiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        mIsThirdPartyIntent =
            mIsPickVideoIntent || mIsPickAudioIntent || mIsGetVideoContentIntent || mIsGetAudioContentIntent ||
                    mIsGetAnyContentIntent

        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(
            binding.directoriesCoordinator,
            binding.directoriesGrid,
            useTransparentNavigation = true,
            useTopSearchMenu = true
        )

        binding.directoriesRefreshLayout.setOnRefreshListener { getDirectories() }
        storeStateVariables()
        checkWhatsNewDialog()

        mIsPasswordProtectionPending = config.isAppPasswordProtectionOn
        setupLatestMediaId()


        if (!config.wasSortingByNumericValueAdded) {
            config.wasSortingByNumericValueAdded = true
            config.sorting = config.sorting or SORT_USE_NUMERIC_VALUE
        }


        updateWidgets()
        registerFileUpdateListener()

        binding.directoriesSwitchSearching.setOnClickListener {
            launchSearchActivity()
        }

        // just request the permission, tryLoadGallery will then trigger in onResume
        handleMediaPermissions { success ->
            if (!success) {
                toast(org.skywaves.mediavox.core.R.string.no_storage_permissions)
                finish()
            }
        }
        handleLogics(binding)
}


    private fun handleMediaPermissions(callback: (granted: Boolean) -> Unit) {
        handlePermission(getPermissionToRequest()) { granted ->
            callback(granted)
            if (granted && isRPlus()) {
                handlePermission(PERMISSION_MEDIA_LOCATION) {}
                if (isTiramisuPlus()) {
                    handlePermission(PERMISSION_READ_MEDIA_VIDEO) {}
                }

                if (!mWasMediaManagementPromptShown) {
                    mWasMediaManagementPromptShown = true
                    handleMediaManagementPrompt { }
                }
            }
        }
        ensureBackgroundThread {
            config.trashItemCount = mediaDB.getDeletedMediaCount()
            config.favCount = mediaDB.getFavoritesCount()
        }
    }

    override fun onStart() {
        super.onStart()
        mTempShowHiddenHandler.removeCallbacksAndMessages(null)
        ensureBackgroundThread {
            config.trashItemCount = mediaDB.getDeletedMediaCount()
            config.favCount = mediaDB.getFavoritesCount()
        }
    }

    override fun onResume() {
        super.onResume()
        updateMenuColors()
        config.isThirdPartyIntent = false
        mDateFormat = config.dateFormat
        mTimeFormat = getTimeFormat()
        handleLogics(binding)
        if (toolShow) {
            updateStatusbarColor(getProperPrimaryColor().adjustAlpha(.6f))
            arrayOf(binding.xyz, binding.mainMenu, binding.mainMenu, binding.directoriesHolder, binding.lastPlayed
            ).forEach {
                it.beGone()
            }
        } else{
            updateStatusbarColor(getProperBackgroundColor())
            arrayOf(binding.xyz, binding.mainMenu, binding.mainMenu, binding.directoriesHolder, binding.lastPlayed
            ).forEach {
                it.beVisible()
            }
            binding.mainContentContainer.beGone()
        }

            refreshMenuItems()

        if (mStoredLastPlayed != config.lastPlayed) {
            getRecyclerAdapter()?.updateLastPlayed(config.lastPlayed)
        }

        if (mStoredTextColor != getProperTextColor()) {
            getRecyclerAdapter()?.updateTextColor(getProperTextColor())
        }

        val primaryColor = getProperPrimaryColor()
        if (mStoredPrimaryColor != primaryColor) {
            getRecyclerAdapter()?.updatePrimaryColor()
        }

        val styleString = "${config.showFolderMediaCount}${config.limitFolderTitle}${config.showDirSize}"
        if (mStoredStyleString != styleString) {
            setupAdapter(mDirs, forceRecreate = true)
        }

        binding.directoriesFastscroller.updateColors(primaryColor)
        binding.directoriesRefreshLayout.isEnabled = true
        getRecyclerAdapter()?.apply {
            dateFormat = config.dateFormat
            timeFormat = getTimeFormat()
        }

        binding.directoriesEmptyPlaceholder.setTextColor(getProperTextColor())
        binding.directoriesEmptyPlaceholder2.setTextColor(primaryColor)
        binding.directoriesSwitchSearching.setTextColor(primaryColor)
        binding.directoriesSwitchSearching.underlineText()
        binding.directoriesEmptyPlaceholder2.bringToFront()

        if (!binding.mainMenu.isSearchOpen) {
            refreshMenuItems()
            if (mIsPasswordProtectionPending && !mWasProtectionHandled) {
                handleAppPasswordProtection {
                    mWasProtectionHandled = it
                    if (it) {
                        mIsPasswordProtectionPending = false
                        tryLoadGallery()
                    } else {
                        finish()
                    }
                }
            } else {
                tryLoadGallery()
            }
        }

        if (config.searchAllFilesByDefault) {
            binding.mainMenu.updateHintText(getString(org.skywaves.mediavox.core.R.string.search_files))
        } else {
            binding.mainMenu.updateHintText(getString(org.skywaves.mediavox.core.R.string.search_folders))
        }
        ensureBackgroundThread {
            config.trashItemCount = mediaDB.getDeletedMediaCount()
            config.favCount = mediaDB.getFavoritesCount()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.directoriesRefreshLayout.isRefreshing = false
        mIsGettingDirs = false
        storeStateVariables()
        handleLogics(binding)
        mLastMediaHandler.removeCallbacksAndMessages(null)
        ensureBackgroundThread {
            config.trashItemCount = mediaDB.getDeletedMediaCount()
            config.favCount = mediaDB.getFavoritesCount()
        }
    }

    override fun onStop() {
        super.onStop()

        if (config.temporarilyShowHidden || config.tempSkipDeleteConfirmation || config.temporarilyShowExcluded) {
            mTempShowHiddenHandler.postDelayed({
                config.temporarilyShowHidden = false
                config.temporarilyShowExcluded = false
                config.tempSkipDeleteConfirmation = false
                config.tempSkipRecycleBin = false
            }, SHOW_TEMP_HIDDEN_DURATION)
        } else {
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
        }
        ensureBackgroundThread {
            config.trashItemCount = mediaDB.getDeletedMediaCount()
            config.favCount = mediaDB.getFavoritesCount()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            config.temporarilyShowHidden = false
            config.temporarilyShowExcluded = false
            config.tempSkipDeleteConfirmation = false
            config.tempSkipRecycleBin = false
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
            removeTempFolder()
            unregisterFileUpdateListener()

            if (!config.showAll) {
                mLastMediaFetcher?.shouldStop = true
                GalleryDatabase.destroyInstance()
            }
        }
        ensureBackgroundThread {
            config.trashItemCount = mediaDB.getDeletedMediaCount()
            config.favCount = mediaDB.getFavoritesCount()
        }
    }


    private fun handleLogics(binding: ActivityMainBinding){
        binding.appNameHome.setTextColor(getProperBackgroundColor().getContrastColor())
        setupFilterFunction(binding)

        binding.lastPlayed.beVisibleIf(config.lastPlayed != "")
        binding.lastPlayed.setMaxImageSize(82)
        if (config.lastPlayedType == "Audio") binding.lastPlayed.setImageResource(R.drawable.ic_audio) else binding.lastPlayed.setImageResource(R.drawable.ic_video)
        binding.lastPlayed.setOnClickListener {
            Intent(this, ViewPagerActivity::class.java).apply {
                putExtra(PATH, config.lastPlayed)
                putExtra(SHOW_FAVORITES, config.lastPlayed == FAVORITES)
                putExtra(SHOW_RECYCLE_BIN, config.lastPlayed == RECYCLE_BIN)
                putExtra(IS_FROM_GALLERY, true)
                startActivity(this)
            }
        }

        binding.moreFeaturesShow.setOnClickListener {
            toolShow = true
            binding.mainContentContainer.beVisible()
            updateStatusbarColor(getProperPrimaryColor().adjustAlpha(.6f))
            arrayOf(
               binding.xyz,
               binding.mainMenu,
               binding.mainMenu,
               binding.directoriesHolder,
               binding.lastPlayed
           ).forEach {
               it.beGone()
           }
            mFragmentManager!!.beginTransaction()
                .replace(R.id.main_content_container, ToolsFragment.instance!!, ToolsFragment.TAG)
                .addToBackStack(null)
                .commit();
        }
    }


    override fun onBackPressed() {
        if (toolShow){
            toolShow = false
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_content_container)
            if ((supportFragmentManager.findFragmentById(R.id.main_content_container)) is ToolsFragment){
                (currentFragment as ToolsFragment).handleBackPressed()
            }
        }
      else  if (binding.mainMenu.isSearchOpen) {
            binding.mainMenu.closeSearch()
        } else if (config.groupDirectSubfolders) {
            if (mCurrentPathPrefix.isEmpty()) {
                super.onBackPressed()
            } else {
                mOpenedSubfolders.removeLast()
                mCurrentPathPrefix = mOpenedSubfolders.last()
                setupAdapter(mDirs)
            }
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_MEDIA && resultData != null) {
                val resultIntent = Intent()
                var resultUri: Uri? = null
                if (mIsThirdPartyIntent) {
                    when {
                        intent.extras?.containsKey(MediaStore.EXTRA_OUTPUT) == true && intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0 -> {
                            resultUri = fillExtraOutput(resultData)
                        }

                        resultData.extras?.containsKey(PICKED_PATHS) == true -> fillPickedPaths(resultData, resultIntent)
                        else -> fillIntentPath(resultData, resultIntent)
                    }
                }

                if (resultUri != null) {
                    resultIntent.data = resultUri
                    resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun refreshMenuItems() {
        if (!mIsThirdPartyIntent) {
            binding.mainMenu.getToolbar().menu.apply {
                findItem(R.id.column_count).isVisible = config.viewTypeFolders == VIEW_TYPE_GRID
                findItem(R.id.set_as_default_folder).isVisible = !config.defaultFolder.isEmpty()
                findItem(R.id.open_recycle_bin).isVisible = config.useRecycleBin
            }
        }

        binding.mainMenu.getToolbar().menu.apply {
            findItem(R.id.temporarily_show_hidden).isVisible = !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible = (!isRPlus() || isExternalStorageManager()) && config.temporarilyShowHidden

            findItem(R.id.temporarily_show_excluded).isVisible = !config.temporarilyShowExcluded
            findItem(R.id.stop_showing_excluded).isVisible = config.temporarilyShowExcluded
        }
    }

    private fun setupOptionsMenu() {
        val menuId = if (mIsThirdPartyIntent) {
            R.menu.menu_main_intent
        } else {
            R.menu.menu_main
        }

        binding.mainMenu.getToolbar().inflateMenu(menuId)
        binding.mainMenu.toggleHideOnScroll(true)
        binding.mainMenu.setupMenu()

        binding.mainMenu.onSearchOpenListener = {
            if (config.searchAllFilesByDefault) {
                launchSearchActivity()
            }
        }

        binding.mainMenu.onSearchTextChangedListener = { text ->
            setupAdapter(mDirsIgnoringSearch, text)
            binding.directoriesRefreshLayout.isEnabled = text.isEmpty()
            binding.directoriesSwitchSearching.beVisibleIf(text.isNotEmpty())
        }

        binding.mainMenu.getToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.filter -> showFilterMediaDialog()
                R.id.show_all -> showAllMedia()
                R.id.change_view_type -> changeViewType()
                R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
                R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
                R.id.temporarily_show_excluded -> tryToggleTemporarilyShowExcluded()
                R.id.stop_showing_excluded -> tryToggleTemporarilyShowExcluded()
                R.id.create_new_folder -> createNewFolder()
                R.id.open_recycle_bin -> openRecycleBin()
                R.id.column_count -> changeColumnCount()
                R.id.set_as_default_folder -> setAsDefaultFolder()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(WAS_PROTECTION_HANDLED, mWasProtectionHandled)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mWasProtectionHandled = savedInstanceState.getBoolean(WAS_PROTECTION_HANDLED, false)
    }

    private fun updateMenuColors() {
        updateStatusbarColor(getProperBackgroundColor())
        binding.mainMenu.updateColors()
    }

    private fun getRecyclerAdapter() = binding.directoriesGrid.adapter as? DirectoryAdapter

    private fun storeStateVariables() {
        mStoredTextColor = getProperTextColor()
        mStoredPrimaryColor = getProperPrimaryColor()
        config.apply {
            mStoredLastPlayed = lastPlayed
            mStoredStyleString = "$showFolderMediaCount$limitFolderTitle$showDirSize"
        }
    }

    private fun startNewPhotoFetcher() {
        if (isNougatPlus()) {
            val photoFetcher = NewPhotoFetcher()
            if (!photoFetcher.isScheduled(applicationContext)) {
                photoFetcher.scheduleJob(applicationContext)
            }
        }
    }

    private fun removeTempFolder() {
        if (config.tempFolderPath.isNotEmpty()) {
            val newFolder = File(config.tempFolderPath)
            if (getDoesFilePathExist(newFolder.absolutePath) && newFolder.isDirectory) {
                if (newFolder.getProperSize(true) == 0L && newFolder.getFileCount(true) == 0 && newFolder.list()?.isEmpty() == true) {
                    toast(String.format(getString(org.skywaves.mediavox.core.R.string.deleting_folder), config.tempFolderPath), Toast.LENGTH_LONG)
                    tryDeleteFileDirItem(newFolder.toFileDirItem(applicationContext), true, true)
                }
            }
            config.tempFolderPath = ""
        }
    }

    private fun setupFilterFunction(binding: ActivityMainBinding) {
        val handleMFSwitch = binding.handleMediaFilter
        handleMFSwitch.switchBkgMiddleColor= getProperTextColor().adjustAlpha(MEDIUM_ALPHA)
        handleMFSwitch.switchToggleMiddleColor = getProperTextColor()
        handleMFSwitch.switchToggleMiddleDrawableRes.setTint(getProperBackgroundColor())

        handleMFSwitch.switchBkgLeftColor= getProperTextColor().adjustAlpha(MEDIUM_ALPHA)
        handleMFSwitch.switchToggleLeftColor = getProperTextColor()
        handleMFSwitch.switchToggleLeftDrawable.setTint(getProperBackgroundColor())

        handleMFSwitch.switchBkgRightColor= getProperTextColor().adjustAlpha(MEDIUM_ALPHA)
        handleMFSwitch.switchToggleRightColor = getProperTextColor()
        handleMFSwitch.switchToggleRightDrawableRes.setTint(getProperBackgroundColor())
        handleMFSwitch.state = when (config.filterMedia) {
            TYPE_AUDIOS -> {
                STATE_LEFT
            }
            TYPE_VIDEOS -> {
                STATE_RIGHT
            }
            else -> {
                STATE_MIDDLE
            }
        }


        handleMFSwitch.addSwitchObserver { switchView, state ->
            when (state) {
                STATE_LEFT -> {
                    config.filterMedia = TYPE_AUDIOS
                    binding.directoriesRefreshLayout.isRefreshing = true
                    binding.directoriesGrid.adapter = null
                    getDirectories()
                }
                STATE_RIGHT -> {
                    config.filterMedia= TYPE_VIDEOS
                    binding.directoriesRefreshLayout.isRefreshing = true
                    binding.directoriesGrid.adapter = null
                    getDirectories()
                }
                else -> {
                    config.filterMedia = getDefaultFileFilter()
                    binding.directoriesRefreshLayout.isRefreshing = true
                    binding.directoriesGrid.adapter = null
                    getDirectories()
                }
            }
        }
    }

    private fun checkOTGPath() {
        ensureBackgroundThread {
            if (!config.wasOTGHandled && hasPermission(getPermissionToRequest()) && hasOTGConnected() && config.OTGPath.isEmpty()) {
                getStorageDirectories().firstOrNull { it.trimEnd('/') != internalStoragePath && it.trimEnd('/') != sdCardPath }?.apply {
                    config.wasOTGHandled = true
                    val otgPath = trimEnd('/')
                    config.OTGPath = otgPath
                    config.addIncludedFolder(otgPath)
                }
            }
        }
    }

    private fun checkDefaultSpamFolders() {
        if (!config.spamFoldersChecked) {
            val spamFolders = arrayListOf(
                "/storage/emulated/0/Android/data/com.facebook.orca/files/stickers"
            )

            val OTGPath = config.OTGPath
            spamFolders.forEach {
                if (getDoesFilePathExist(it, OTGPath)) {
                    config.addExcludedFolder(it)
                }
            }
            config.spamFoldersChecked = true
        }
    }

    private fun tryLoadGallery() {
        // avoid calling anything right after granting the permission, it will be called from onResume()
        val wasMissingPermission = config.appRunCount == 1 && !hasPermission(getPermissionToRequest())
        handleMediaPermissions { success ->
            if (success) {
                if (wasMissingPermission) {
                    return@handleMediaPermissions
                }

                if (!mWasDefaultFolderChecked) {
                    openDefaultFolder()
                    mWasDefaultFolderChecked = true
                }

                checkOTGPath()
                checkDefaultSpamFolders()

                if (config.showAll) {
                    showAllMedia()
                } else {
                    getDirectories()
                }

                setupLayoutManager()
            } else {
                toast(org.skywaves.mediavox.core.R.string.no_storage_permissions)
                finish()
            }
        }
    }

    private fun getDirectories() {
        if (mIsGettingDirs) {
            return
        }

        mShouldStopFetching = true
        mIsGettingDirs = true
        val getVideosOnly = mIsPickVideoIntent || mIsGetVideoContentIntent
        val getAudiosOnly = mIsPickAudioIntent || mIsGetAudioContentIntent

        getCachedDirectories(getAudiosOnly, getVideosOnly) {
            gotDirectories(addTempFolderIfNeeded(it))
        }
    }

    private fun launchSearchActivity() {
        hideKeyboard()
        Intent(this, SearchActivity::class.java).apply {
            startActivity(this)
        }

        binding.mainMenu.postDelayed({
            binding.mainMenu.closeSearch()
        }, 500)
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(this, true, false) {
            binding.directoriesGrid.adapter = null
            if (config.directorySorting and SORT_BY_DATE_MODIFIED != 0 || config.directorySorting and SORT_BY_DATE_TAKEN != 0) {
                getDirectories()
            } else {
                ensureBackgroundThread {
                    gotDirectories(getCurrentlyDisplayedDirs())
                }
            }

            getRecyclerAdapter()?.directorySorting = config.directorySorting
        }
    }

    private fun showFilterMediaDialog() {
        FilterMediaDialog(this) {
            mShouldStopFetching = true
            binding.directoriesRefreshLayout.isRefreshing = true
            binding.directoriesGrid.adapter = null
            getDirectories()
        }
    }

    private fun showAllMedia() {
        config.showAll = true
        Intent(this, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, "")

            if (mIsThirdPartyIntent) {
                handleMediaIntent(this)
            } else {
                hideKeyboard()
                startActivity(this)
                finish()
            }
        }
    }

    private fun changeViewType() {
        ChangeViewTypeDialog(this, true) {
            refreshMenuItems()
            setupLayoutManager()
            binding.directoriesGrid.adapter = null
            setupAdapter(getRecyclerAdapter()?.dirs ?: mDirs)
        }
    }

    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            if (isRPlus() && !isExternalStorageManager()) {
                GrantAllFilesDialog(this)
            } else {
                handleHiddenFolderPasswordProtection {
                    toggleTemporarilyShowHidden(true)
                }
            }
        }
    }

    private fun toggleTemporarilyShowHidden(show: Boolean) {
        mLoadedInitialPhotos = false
        config.temporarilyShowHidden = show
        binding.directoriesGrid.adapter = null
        getDirectories()
        refreshMenuItems()
    }

    private fun tryToggleTemporarilyShowExcluded() {
        if (config.temporarilyShowExcluded) {
            toggleTemporarilyShowExcluded(false)
        } else {
            handleExcludedFolderPasswordProtection {
                toggleTemporarilyShowExcluded(true)
            }
        }
    }

    private fun toggleTemporarilyShowExcluded(show: Boolean) {
        mLoadedInitialPhotos = false
        config.temporarilyShowExcluded = show
        binding.directoriesGrid.adapter = null
        getDirectories()
        refreshMenuItems()
    }

    override fun deleteFolders(folders: ArrayList<File>) {
        val fileDirItems =
            folders.asSequence().filter { it.isDirectory }.map { FileDirItem(it.absolutePath, it.name, true) }.toMutableList() as ArrayList<FileDirItem>
        when {
            fileDirItems.isEmpty() -> return
            fileDirItems.size == 1 -> {
                try {
                    toast(String.format(getString(org.skywaves.mediavox.core.R.string.deleting_folder), fileDirItems.first().name))
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }

            else -> {
                val baseString =
                    if (config.useRecycleBin && !config.tempSkipRecycleBin) org.skywaves.mediavox.core.R.plurals.moving_items_into_bin else org.skywaves.mediavox.core.R.plurals.delete_items
                val deletingItems = resources.getQuantityString(baseString, fileDirItems.size, fileDirItems.size)
                toast(deletingItems)
            }
        }

        val itemsToDelete = ArrayList<FileDirItem>()
        val filter = config.filterMedia
        val showHidden = config.shouldShowHidden
        fileDirItems.filter { it.isDirectory }.forEach {
            val files = File(it.path).listFiles()
            files?.filter {
                it.absolutePath.isMediaFile() && (showHidden || !it.name.startsWith('.')) &&
                    ((it.isVideoFast() && filter and TYPE_VIDEOS != 0) ||
                        (it.isAudioFast() && filter and TYPE_AUDIOS != 0))
            }?.mapTo(itemsToDelete) { it.toFileDirItem(applicationContext) }
        }

        if (config.useRecycleBin && !config.tempSkipRecycleBin) {
            val pathsToDelete = ArrayList<String>()
            itemsToDelete.mapTo(pathsToDelete) { it.path }

            movePathsInRecycleBin(pathsToDelete) {
                if (it) {
                    deleteFilteredFileDirItems(itemsToDelete, folders)
                } else {
                    toast(org.skywaves.mediavox.core.R.string.unknown_error_occurred)
                }
            }
        } else {
            deleteFilteredFileDirItems(itemsToDelete, folders)
        }
    }

    private fun deleteFilteredFileDirItems(fileDirItems: ArrayList<FileDirItem>, folders: ArrayList<File>) {
        val OTGPath = config.OTGPath
        deleteFiles(fileDirItems) {
            runOnUiThread {
                refreshItems()
            }

            ensureBackgroundThread {
                folders.filter { !getDoesFilePathExist(it.absolutePath, OTGPath) }.forEach {
                    directoryDB.deleteDirPath(it.absolutePath)
                }

                if (config.deleteEmptyFolders) {
                    folders.filter { !it.absolutePath.isDownloadsFolder() && it.isDirectory && it.toFileDirItem(this).getProperFileCount(this, true) == 0 }
                        .forEach {
                            tryDeleteFileDirItem(it.toFileDirItem(this), true, true)
                        }
                }
            }
        }
    }

    private fun setupLayoutManager() {
        if (config.viewTypeFolders == VIEW_TYPE_GRID) {
            setupGridLayoutManager()
        } else {
            setupListLayoutManager()
        }

        (binding.directoriesRefreshLayout.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.BELOW, R.id.directories_switch_searching)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.directoriesRefreshLayout.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutManager.spanCount = config.dirColumnCnt
    }

    private fun setupListLayoutManager() {
        val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.directoriesRefreshLayout.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    private fun createNewFolder() {
        FilePickerDialog(this, internalStoragePath, false, config.shouldShowHidden, false, true) {
            CreateNewFolderDialog(this, it) {
                config.tempFolderPath = it
                ensureBackgroundThread {
                    gotDirectories(addTempFolderIfNeeded(getCurrentlyDisplayedDirs()))
                }
            }
        }
    }

    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..MAX_COLUMN_COUNT) {
            items.add(RadioItem(i+1, resources.getQuantityString(org.skywaves.mediavox.core.R.plurals.column_counts, i, i+1)))
        }

        val currentColumnCount = (binding.directoriesGrid.layoutManager as MyGridLayoutManager).spanCount
        RadioGroupDialog(this, items, currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.dirColumnCnt = newColumnCount
                columnCountChanged()
            }
        }
    }

    private fun increaseColumnCount() {
        config.dirColumnCnt += 1
        columnCountChanged()
    }

    private fun reduceColumnCount() {
        config.dirColumnCnt -= 1
        columnCountChanged()
    }

    private fun columnCountChanged() {
        (binding.directoriesGrid.layoutManager as MyGridLayoutManager).spanCount = config.dirColumnCnt
        refreshMenuItems()
        getRecyclerAdapter()?.apply {
            notifyItemRangeChanged(0, dirs.size)
        }
    }

    private fun isPickVideoIntent(intent: Intent) = isPickIntent(intent) && (hasVideoContentData(intent) || isVideoType(intent))

    private fun isPickAudioIntent(intent: Intent) = isPickIntent(intent) && (hasAudioContentData(intent) || isAudioType(intent))

    private fun isPickIntent(intent: Intent) = intent.action == Intent.ACTION_PICK

    private fun isGetContentIntent(intent: Intent) = intent.action == Intent.ACTION_GET_CONTENT && intent.type != null

    private fun isGetVideoContentIntent(intent: Intent) = isGetContentIntent(intent) &&
        (intent.type!!.startsWith("video/") || intent.type == Video.Media.CONTENT_TYPE)

    private fun isGetAudioContentIntent(intent: Intent) = isGetContentIntent(intent) &&
            (intent.type!!.startsWith("audio/") || intent.type == Audio.Media.CONTENT_TYPE)

    private fun isGetAnyContentIntent(intent: Intent) = isGetContentIntent(intent) && intent.type == "*/*"

    private fun hasVideoContentData(intent: Intent) = (intent.data == Video.Media.EXTERNAL_CONTENT_URI ||
        intent.data == Video.Media.INTERNAL_CONTENT_URI)

    private fun hasAudioContentData(intent: Intent) = (intent.data == Audio.Media.EXTERNAL_CONTENT_URI ||
            intent.data == Audio.Media.INTERNAL_CONTENT_URI)

    private fun isVideoType(intent: Intent) = (intent.type?.startsWith("video/") == true || intent.type == Video.Media.CONTENT_TYPE)

    private fun isAudioType(intent: Intent) = (intent.type?.startsWith("audio/") == true || intent.type == Audio.Media.CONTENT_TYPE)

    private fun fillExtraOutput(resultData: Intent): Uri? {
        val file = File(resultData.data!!.path!!)
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val output = intent.extras!!.get(MediaStore.EXTRA_OUTPUT) as Uri
            inputStream = FileInputStream(file)
            outputStream = contentResolver.openOutputStream(output)
            inputStream.copyTo(outputStream!!)
        } catch (e: SecurityException) {
            showErrorToast(e)
        } catch (ignored: FileNotFoundException) {
            return getFilePublicUri(file, BuildConfig.APPLICATION_ID)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        return null
    }

    private fun fillPickedPaths(resultData: Intent, resultIntent: Intent) {
        val paths = resultData.extras!!.getStringArrayList(PICKED_PATHS)
        val uris = paths!!.map { getFilePublicUri(File(it), BuildConfig.APPLICATION_ID) } as ArrayList
        val clipData = ClipData("Attachment", arrayOf("video/*", "audio/*"), ClipData.Item(uris.removeAt(0)))

        uris.forEach {
            clipData.addItem(ClipData.Item(it))
        }

        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        resultIntent.clipData = clipData
    }

    private fun fillIntentPath(resultData: Intent, resultIntent: Intent) {
        val data = resultData.data
        val path = if (data.toString().startsWith("/")) data.toString() else data!!.path
        val uri = getFilePublicUri(File(path!!), BuildConfig.APPLICATION_ID)
        val type = path.getMimeType()
        resultIntent.setDataAndTypeAndNormalize(uri, type)
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun itemClicked(path: String) {
        handleLockedFolderOpening(path) { success ->
            if (success) {
                Intent(this, MediaActivity::class.java).apply {
                    putExtra(SKIP_AUTHENTICATION, true)
                    putExtra(DIRECTORY, path)
                    handleMediaIntent(this)
                }
            }
        }
    }

    private fun handleMediaIntent(intent: Intent) {
        hideKeyboard()
        intent.apply {
                putExtra(GET_VIDEO_INTENT, mIsPickVideoIntent || mIsGetVideoContentIntent)
                putExtra(GET_AUDIO_INTENT, mIsPickAudioIntent || mIsGetAudioContentIntent)
                putExtra(GET_ANY_INTENT, mIsGetAnyContentIntent)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, mAllowPickingMultiple)
                startActivityForResult(this, PICK_MEDIA)
        }
    }

    private fun gotDirectories(newDirs: ArrayList<Directory>) {
        mIsGettingDirs = false
        mShouldStopFetching = false

        // if hidden item showing is disabled but all Favorite items are hidden, hide the Favorites folder
        if (!config.shouldShowHidden) {
            val favoritesFolder = newDirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null && favoritesFolder.tmb.getFilenameFromPath().startsWith('.')) {
                newDirs.remove(favoritesFolder)
            }
        }

        val dirs = getSortedDirectories(newDirs)
        if (config.groupDirectSubfolders) {
            mDirs = dirs.clone() as ArrayList<Directory>
        }

        var isPlaceholderVisible = dirs.isEmpty()

        runOnUiThread {
            checkPlaceholderVisibility(dirs)
            setupAdapter(dirs.clone() as ArrayList<Directory>)
        }

        // cached folders have been loaded, recheck folders one by one starting with the first displayed
        mLastMediaFetcher?.shouldStop = true
        mLastMediaFetcher = MediaFetcher(applicationContext)
        val getVideosOnly = mIsPickVideoIntent || mIsGetVideoContentIntent
        val getAudiosOnly = mIsPickAudioIntent || mIsGetAudioContentIntent
        val favoritePaths = getFavoritePaths()
        val hiddenString = getString(R.string.hidden)
        val albumCovers = config.parseAlbumCovers()
        val includedFolders = config.includedFolders
        val noMediaFolders = getNoMediaFoldersSync()
        val tempFolderPath = config.tempFolderPath
        val getProperFileSize = true
        val dirPathsToRemove = ArrayList<String>()
        val lastModifieds = mLastMediaFetcher!!.getLastModifieds()
        val dateTakens = mLastMediaFetcher!!.getDateTakens()

        // fetch files from MediaStore only, unless the app has the MANAGE_EXTERNAL_STORAGE permission on Android 11+
        val android11Files = mLastMediaFetcher?.getAndroid11FolderMedia(getVideosOnly, getAudiosOnly, favoritePaths, false, true, dateTakens)
        try {
            for (directory in dirs) {
                if (mShouldStopFetching || isDestroyed || isFinishing) {
                    return
                }

                val sorting = config.getFolderSorting(directory.path)
                val grouping = config.getFolderGrouping(directory.path)
                val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
                    sorting and SORT_BY_DATE_TAKEN != 0 ||
                    grouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
                    grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

                val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
                    sorting and SORT_BY_DATE_MODIFIED != 0 ||
                    grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
                    grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

                val curMedia = mLastMediaFetcher!!.getFilesFrom(
                    directory.path, getVideosOnly, getAudiosOnly, getProperDateTaken, getProperLastModified,
                    getProperFileSize, favoritePaths, false, lastModifieds, dateTakens, android11Files
                )

                val newDir = if (curMedia.isEmpty()) {
                    if (directory.path != tempFolderPath) {
                        dirPathsToRemove.add(directory.path)
                    }
                    directory
                } else {
                    createDirectoryFromMedia(directory.path, curMedia, albumCovers, hiddenString, includedFolders, getProperFileSize, noMediaFolders)
                }

                // we are looping through the already displayed folders looking for changes, do not do anything if nothing changed
                if (directory.copy(subfoldersCount = 0, subfoldersMediaCount = 0) == newDir) {
                    continue
                }

                directory.apply {
                    tmb = newDir.tmb
                    name = newDir.name
                    mediaCnt = newDir.mediaCnt
                    modified = newDir.modified
                    taken = newDir.taken
                    this@apply.size = newDir.size
                    types = newDir.types
                    sortValue = getDirectorySortingValue(curMedia, path, name, size)
                }

                setupAdapter(dirs)

                // update directories and media files in the local db, delete invalid items. Intentionally creating a new thread
                updateDBDirectory(directory)
                if (!directory.isRecycleBin() && !directory.areFavorites()) {
                    Thread {
                        try {
                            mediaDB.insertAll(curMedia)
                        } catch (ignored: Exception) {
                        }
                    }.start()
                }

                if (!directory.isRecycleBin()) {
                    getCachedMedia(directory.path, getAudiosOnly, getVideosOnly) {
                        val mediaToDelete = ArrayList<Medium>()
                        it.forEach {
                            if (!curMedia.contains(it)) {
                                val medium = it as? Medium
                                val path = medium?.path
                                if (path != null) {
                                    mediaToDelete.add(medium)
                                }
                            }
                        }
                        mediaDB.deleteMedia(*mediaToDelete.toTypedArray())
                    }
                }
            }

            if (dirPathsToRemove.isNotEmpty()) {
                val dirsToRemove = dirs.filter { dirPathsToRemove.contains(it.path) }
                dirsToRemove.forEach {
                    directoryDB.deleteDirPath(it.path)
                }
                dirs.removeAll(dirsToRemove)
                setupAdapter(dirs)
            }
        } catch (ignored: Exception) {
        }

        val foldersToScan = mLastMediaFetcher!!.getFoldersToScan()
        dirs.filterNot { it.path == RECYCLE_BIN || it.path == FAVORITES }.forEach {
            foldersToScan.remove(it.path)
        }

        // check the remaining folders which were not cached at all yet
        for (folder in foldersToScan) {
            if (mShouldStopFetching || isDestroyed || isFinishing) {
                return
            }

            val sorting = config.getFolderSorting(folder)
            val grouping = config.getFolderGrouping(folder)
            val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
                sorting and SORT_BY_DATE_TAKEN != 0 ||
                grouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
                grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

            val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
                sorting and SORT_BY_DATE_MODIFIED != 0 ||
                grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
                grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

            val newMedia = mLastMediaFetcher!!.getFilesFrom(
                folder, getVideosOnly, getAudiosOnly, getProperDateTaken, getProperLastModified,
                getProperFileSize, favoritePaths, false, lastModifieds, dateTakens, android11Files
            )

            if (newMedia.isEmpty()) {
                continue
            }

            if (isPlaceholderVisible) {
                isPlaceholderVisible = false
                runOnUiThread {
                    binding.directoriesEmptyPlaceholder.beGone()
                    binding.directoriesEmptyPlaceholder2.beGone()
                    binding.directoriesFastscroller.beVisible()
                }
            }

            val newDir = createDirectoryFromMedia(folder, newMedia, albumCovers, hiddenString, includedFolders, getProperFileSize, noMediaFolders)
            dirs.add(newDir)
            setupAdapter(dirs)

            // make sure to create a new thread for these operations, dont just use the common bg thread
            Thread {
                try {
                    directoryDB.insert(newDir)
                    if (folder != RECYCLE_BIN && folder != FAVORITES) {
                        mediaDB.insertAll(newMedia)
                    }
                } catch (ignored: Exception) {
                }
            }.start()
        }

        mLoadedInitialPhotos = true
        if (config.appRunCount > 1) {
            checkLastMediaChanged()
        }

        runOnUiThread {
            binding.directoriesRefreshLayout.isRefreshing = false
            checkPlaceholderVisibility(dirs)
        }

        checkInvalidDirectories(dirs)
        if (mDirs.size > 50) {
            excludeSpamFolders()
        }

        val excludedFolders = config.excludedFolders
        val everShownFolders = config.everShownFolders.toMutableSet() as HashSet<String>

        // do not add excluded folders and their subfolders at everShownFolders
        dirs.filter { dir ->
            return@filter !excludedFolders.any { dir.path.startsWith(it) }
        }.mapTo(everShownFolders) { it.path }

        try {
            // scan the internal storage from time to time for new folders
            if (config.appRunCount == 1 || config.appRunCount % 30 == 0) {
                everShownFolders.addAll(getFoldersWithMedia(config.internalStoragePath))
            }

            // catch some extreme exceptions like too many everShownFolders for storing, shouldnt really happen
            config.everShownFolders = everShownFolders
        } catch (e: Exception) {
            config.everShownFolders = HashSet()
        }

        mDirs = dirs.clone() as ArrayList<Directory>
    }

    private fun setAsDefaultFolder() {
        config.defaultFolder = ""
        refreshMenuItems()
    }

    private fun openDefaultFolder() {
        if (config.defaultFolder.isEmpty()) {
            return
        }

        val defaultDir = File(config.defaultFolder)

        if ((!defaultDir.exists() || !defaultDir.isDirectory) && (config.defaultFolder != RECYCLE_BIN && config.defaultFolder != FAVORITES)) {
            config.defaultFolder = ""
            return
        }

        Intent(this, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, config.defaultFolder)
            handleMediaIntent(this)
        }
    }

    private fun checkPlaceholderVisibility(dirs: ArrayList<Directory>) {
        binding.directoriesEmptyPlaceholder.beVisibleIf(dirs.isEmpty() && mLoadedInitialPhotos)
        binding.directoriesEmptyPlaceholder2.beVisibleIf(dirs.isEmpty() && mLoadedInitialPhotos)

        if (binding.mainMenu.isSearchOpen) {
            binding.directoriesEmptyPlaceholder.text = getString(org.skywaves.mediavox.core.R.string.no_items_found)
            binding.directoriesEmptyPlaceholder2.beGone()
        } else if (dirs.isEmpty() && config.filterMedia == getDefaultFileFilter()) {
            if (isRPlus() && !isExternalStorageManager()) {
                binding.directoriesEmptyPlaceholder.text = getString(org.skywaves.mediavox.core.R.string.no_items_found)
                binding.directoriesEmptyPlaceholder2.beGone()
            } else {
                binding.directoriesEmptyPlaceholder.text = getString(R.string.no_media_add_included)
                binding.directoriesEmptyPlaceholder2.text = getString(R.string.add_folder)
            }

            binding.directoriesEmptyPlaceholder2.setOnClickListener {
                showAddIncludedFolderDialog {
                    refreshItems()
                }
            }
        } else {
            binding.directoriesEmptyPlaceholder.text = getString(R.string.no_media_with_filters)
            binding.directoriesEmptyPlaceholder2.text = getString(R.string.change_filters_underlined)

            binding.directoriesEmptyPlaceholder2.setOnClickListener {
                showFilterMediaDialog()
            }
        }

        binding.directoriesEmptyPlaceholder2.underlineText()
        binding.directoriesFastscroller.beVisibleIf(binding.directoriesEmptyPlaceholder.isGone())
    }

    private fun setupAdapter(dirs: ArrayList<Directory>, textToSearch: String = binding.mainMenu.getCurrentQuery(), forceRecreate: Boolean = false) {
        val currAdapter = binding.directoriesGrid.adapter
        val distinctDirs = dirs.distinctBy { it.path.getDistinctPath() }.toMutableList() as ArrayList<Directory>
        val sortedDirs = getSortedDirectories(distinctDirs)
        var dirsToShow = getDirsToShow(sortedDirs, mDirs, mCurrentPathPrefix).clone() as ArrayList<Directory>

        if (currAdapter == null || forceRecreate) {
            mDirsIgnoringSearch = dirs
            DirectoryAdapter(
                this,
                dirsToShow,
                this,
                binding.directoriesGrid,
                isPickIntent(intent) || isGetAnyContentIntent(intent),
                binding.directoriesRefreshLayout
            ) {
                val clickedDir = it as Directory
                val path = clickedDir.path
                if (clickedDir.subfoldersCount == 1 || !config.groupDirectSubfolders) {
                    if (path != config.tempFolderPath) {
                        itemClicked(path)
                    }
                } else {
                    mCurrentPathPrefix = path
                    mOpenedSubfolders.add(path)
                    setupAdapter(mDirs, "")
                }
            }.apply {
                runOnUiThread {
                    binding.directoriesGrid.adapter = this
                    setupScrollDirection()

                    if (config.viewTypeFolders == VIEW_TYPE_LIST && areSystemAnimationsEnabled) {
                        binding.directoriesGrid.scheduleLayoutAnimation()
                    }
                }
            }
        } else {
            runOnUiThread {
                if (textToSearch.isNotEmpty()) {
                    dirsToShow = dirsToShow.filter { it.name.contains(textToSearch, true) }.sortedBy { !it.name.startsWith(textToSearch, true) }
                        .toMutableList() as ArrayList
                }
                checkPlaceholderVisibility(dirsToShow)

                (binding.directoriesGrid.adapter as? DirectoryAdapter)?.updateDirs(dirsToShow)
            }
        }

        // recyclerview sometimes becomes empty at init/update, triggering an invisible refresh like this seems to work fine
        binding.directoriesGrid.postDelayed({
            binding.directoriesGrid.scrollBy(0, 0)
        }, 500)
    }

    private fun setupScrollDirection() {
        binding.directoriesFastscroller.setScrollVertically(true)
    }

    private fun checkInvalidDirectories(dirs: ArrayList<Directory>) {
        val invalidDirs = ArrayList<Directory>()
        val OTGPath = config.OTGPath
        dirs.filter { !it.areFavorites() && !it.isRecycleBin() }.forEach {
            if (!getDoesFilePathExist(it.path, OTGPath)) {
                invalidDirs.add(it)
            } else if (it.path != config.tempFolderPath && (!isRPlus() || isExternalStorageManager())) {
                // avoid calling file.list() or listfiles() on Android 11+, it became way too slow
                val children = if (isPathOnOTG(it.path)) {
                    getOTGFolderChildrenNames(it.path)
                } else {
                    File(it.path).list()?.asList()
                }

                val hasMediaFile = children?.any {
                    it != null && (it.isMediaFile() || (it.startsWith("img_", true) && File(it).isDirectory))
                } ?: false

                if (!hasMediaFile) {
                    invalidDirs.add(it)
                }
            }
        }

        if (getFavoritePaths().isEmpty()) {
            val favoritesFolder = dirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null) {
                invalidDirs.add(favoritesFolder)
            }
        }

        if (config.useRecycleBin) {
            try {
                val binFolder = dirs.firstOrNull { it.path == RECYCLE_BIN }
                if (binFolder != null && mediaDB.getDeletedMedia().isEmpty()) {
                    invalidDirs.add(binFolder)
                }
            } catch (ignored: Exception) {
            }
        }

        if (invalidDirs.isNotEmpty()) {
            dirs.removeAll(invalidDirs)
            setupAdapter(dirs)
            invalidDirs.forEach {
                try {
                    directoryDB.deleteDirPath(it.path)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun getCurrentlyDisplayedDirs() = getRecyclerAdapter()?.dirs ?: ArrayList()

    private fun setupLatestMediaId() {
        ensureBackgroundThread {
            if (hasPermission(PERMISSION_READ_STORAGE)) {
                mLatestMediaId = getLatestMediaId()
                mLatestMediaDateId = getLatestMediaByDateId()
            }
        }
    }

    private fun checkLastMediaChanged() {
        if (isDestroyed) {
            return
        }

        mLastMediaHandler.postDelayed({
            ensureBackgroundThread {
                val mediaId = getLatestMediaId()
                val mediaDateId = getLatestMediaByDateId()
                if (mLatestMediaId != mediaId || mLatestMediaDateId != mediaDateId) {
                    mLatestMediaId = mediaId
                    mLatestMediaDateId = mediaDateId
                    runOnUiThread {
                        getDirectories()
                    }
                } else {
                    mLastMediaHandler.removeCallbacksAndMessages(null)
                    checkLastMediaChanged()
                }
            }
        }, LAST_MEDIA_CHECK_PERIOD)
    }

    private fun checkRecycleBinItems() {
        if (config.useRecycleBin && config.lastBinCheck < System.currentTimeMillis() - DAY_SECONDS * 1000) {
            config.lastBinCheck = System.currentTimeMillis()
            Handler().postDelayed({
                ensureBackgroundThread {
                    try {
                        val filesToDelete = mediaDB.getOldRecycleBinItems(System.currentTimeMillis() - MONTH_MILLISECONDS)
                        filesToDelete.forEach {
                            if (File(it.path.replaceFirst(RECYCLE_BIN, recycleBinPath)).delete()) {
                                mediaDB.deleteMediumPath(it.path)
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }, 3000L)
        }
    }

    // exclude probably unwanted folders, for example facebook stickers are split between hundreds of separate folders like
    // /storage/emulated/0/Android/data/com.facebook.orca/files/stickers/175139712676531/209575122566323
    // /storage/emulated/0/Android/data/com.facebook.orca/files/stickers/497837993632037/499671223448714
    private fun excludeSpamFolders() {
        ensureBackgroundThread {
            try {
                val internalPath = internalStoragePath
                val checkedPaths = ArrayList<String>()
                val oftenRepeatedPaths = ArrayList<String>()
                val paths = mDirs.map { it.path.removePrefix(internalPath) }.toMutableList() as ArrayList<String>
                paths.forEach {
                    val parts = it.split("/")
                    var currentString = ""
                    for (i in 0 until parts.size) {
                        currentString += "${parts[i]}/"

                        if (!checkedPaths.contains(currentString)) {
                            val cnt = paths.count { it.startsWith(currentString) }
                            if (cnt > 50 && currentString.startsWith("/Android/data", true)) {
                                oftenRepeatedPaths.add(currentString)
                            }
                        }

                        checkedPaths.add(currentString)
                    }
                }

                val substringToRemove = oftenRepeatedPaths.filter {
                    val path = it
                    it == "/" || oftenRepeatedPaths.any { it != path && it.startsWith(path) }
                }

                oftenRepeatedPaths.removeAll(substringToRemove)
                val OTGPath = config.OTGPath
                oftenRepeatedPaths.forEach {
                    val file = File("$internalPath/$it")
                    if (getDoesFilePathExist(file.absolutePath, OTGPath)) {
                        config.addExcludedFolder(file.absolutePath)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun getFoldersWithMedia(path: String): HashSet<String> {
        val folders = HashSet<String>()
        try {
            val files = File(path).listFiles()
            if (files != null) {
                files.sortBy { !it.isDirectory }
                for (file in files) {
                    if (file.isDirectory && !file.startsWith("${config.internalStoragePath}/Android")) {
                        folders.addAll(getFoldersWithMedia(file.absolutePath))
                    } else if (file.isFile && file.isMediaFile()) {
                        folders.add(file.parent ?: "")
                        break
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        return folders
    }

    override fun refreshItems() {
        getDirectories()
    }


    override fun recheckPinnedFolders() {
        ensureBackgroundThread {
            gotDirectories(movePinnedDirectoriesToFront(getCurrentlyDisplayedDirs()))
        }
    }

    override fun updateDirectories(directories: ArrayList<Directory>) {
        ensureBackgroundThread {
            storeDirectoryItems(directories)
            removeInvalidDBDirectories()
        }
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
