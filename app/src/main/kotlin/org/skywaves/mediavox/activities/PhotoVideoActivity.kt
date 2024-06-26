package org.skywaves.mediavox.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import org.skywaves.mediavox.core.dialogs.PropertiesDialog
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.core.helpers.*
import org.skywaves.mediavox.BuildConfig
import org.skywaves.mediavox.R
import org.skywaves.mediavox.databinding.FragmentHolderBinding
import org.skywaves.mediavox.extensions.*
import org.skywaves.mediavox.fragments.VideoFragment
import org.skywaves.mediavox.fragments.ViewPagerFragment
import org.skywaves.mediavox.helpers.*
import org.skywaves.mediavox.models.Medium
import java.io.File

open class PhotoVideoActivity : SimpleActivity(), ViewPagerFragment.FragmentListener {
    private var mMedium: Medium? = null
    private var mIsFullScreen = false
    private var mIsFromGallery = false
    private var mFragment: ViewPagerFragment? = null
    private var mUri: Uri? = null

    var mIsVideo = false

    private val binding by viewBinding(FragmentHolderBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        showTransparentTop = true

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupOptionsMenu()
        refreshMenuItems()
        handlePermission(getPermissionToRequest()) {
            if (it) {
                checkIntent(savedInstanceState)
            } else {
                toast(org.skywaves.mediavox.core.R.string.no_storage_permissions)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (config.bottomActions) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            setTranslucentNavigation()
        }

        if (config.blackBackground) {
            updateStatusbarColor(Color.BLACK)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initBottomActionsLayout()

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        (binding.fragmentViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin = statusBarHeight
        if (!portrait && navigationBarOnSide && navigationBarWidth > 0) {
            binding.fragmentViewerToolbar.setPadding(0, 0, navigationBarWidth, 0)
        } else {
            binding.fragmentViewerToolbar.setPadding(0, 0, 0, 0)
        }
    }

    fun refreshMenuItems() {
        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0

        binding.fragmentViewerToolbar.menu.apply {
            findItem(R.id.menu_properties).isVisible = mUri?.scheme == "file" && visibleBottomActions and BOTTOM_ACTION_PROPERTIES == 0
            findItem(R.id.menu_share).isVisible = visibleBottomActions and BOTTOM_ACTION_SHARE == 0
        }
    }

    private fun setupOptionsMenu() {
        (binding.fragmentViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin = statusBarHeight
        binding.fragmentViewerToolbar.apply {
            setTitleTextColor(Color.WHITE)
            overflowIcon = resources.getColoredDrawableWithColor(org.skywaves.mediavox.core.R.drawable.ic_three_dots_vector, Color.WHITE)
            navigationIcon = resources.getColoredDrawableWithColor(org.skywaves.mediavox.core.R.drawable.ic_arrow_left_vector, Color.WHITE)
        }

        updateMenuItemColors(binding.fragmentViewerToolbar.menu, forceWhiteIcons = true)
        binding.fragmentViewerToolbar.setOnMenuItemClickListener { menuItem ->
            if (mMedium == null || mUri == null) {
                return@setOnMenuItemClickListener true
            }

            when (menuItem.itemId) {
                R.id.menu_open_with -> openPath(mUri!!.toString(), true)
                R.id.menu_share -> sharePath(mUri!!.toString())
                R.id.menu_properties -> showProperties()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }

        binding.fragmentViewerToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun checkIntent(savedInstanceState: Bundle? = null) {
        if (intent.data == null && intent.action == Intent.ACTION_VIEW) {
            hideKeyboard()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        mUri = intent.data ?: return
        val uri = mUri.toString()
        if (uri.startsWith("content:/") && uri.contains("/storage/") && !intent.getBooleanExtra(IS_IN_RECYCLE_BIN, false)) {
            val guessedPath = uri.substring(uri.indexOf("/storage/"))
            if (getDoesFilePathExist(guessedPath)) {
                val extras = intent.extras ?: Bundle()
                extras.apply {
                    putString(REAL_FILE_PATH, guessedPath)
                    intent.putExtras(this)
                }
            }
        }

        var filename = getFilenameFromUri(mUri!!)
        mIsFromGallery = intent.getBooleanExtra(IS_FROM_GALLERY, false)
        if (mIsFromGallery && (filename.isVideoFast() || filename.isAudioFast()) && config.openVideosOnSeparateScreen) {
            launchVideoPlayer()
            return
        }

        if (intent.extras?.containsKey(REAL_FILE_PATH) == true) {
            val realPath = intent.extras!!.getString(REAL_FILE_PATH)
            if (realPath != null && getDoesFilePathExist(realPath)) {
                val isFileFolderHidden = (File(realPath).isHidden || File(realPath.getParentPath(), NOMEDIA).exists() || realPath.contains("/."))
                val preventShowingHiddenFile = (isRPlus() && !isExternalStorageManager()) && isFileFolderHidden
                if (!preventShowingHiddenFile) {
                    if (realPath.getFilenameFromPath().contains('.') || filename.contains('.')) {
                        if (isFileTypeVisible(realPath)) {
                            binding.bottomActions.root.beGone()
                            sendViewPagerIntent(realPath)
                            finish()
                            return
                        }
                    } else {
                        filename = realPath.getFilenameFromPath()
                    }
                }
            }
        }

        if (mUri!!.scheme == "file") {
            if (filename.contains('.')) {
                binding.bottomActions.root.beGone()
                rescanPaths(arrayListOf(mUri!!.path!!))
                sendViewPagerIntent(mUri!!.path!!)
                finish()
            }
            return
        } else {
            val realPath = applicationContext.getRealPathFromURI(mUri!!) ?: ""
            val isFileFolderHidden = (File(realPath).isHidden || File(realPath.getParentPath(), NOMEDIA).exists() || realPath.contains("/."))
            val preventShowingHiddenFile = (isRPlus() && !isExternalStorageManager()) && isFileFolderHidden
            if (!preventShowingHiddenFile) {
                if (realPath != mUri.toString() && realPath.isNotEmpty() && mUri!!.authority != "mms" && filename.contains('.') && getDoesFilePathExist(realPath)) {
                    if (isFileTypeVisible(realPath)) {
                        binding.bottomActions.root.beGone()
                        rescanPaths(arrayListOf(mUri!!.path!!))
                        sendViewPagerIntent(realPath)
                        finish()
                        return
                    }
                }
            }
        }

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        if (!portrait && navigationBarOnSide && navigationBarWidth > 0) {
            binding.fragmentViewerToolbar.setPadding(0, 0, navigationBarWidth, 0)
        } else {
            binding.fragmentViewerToolbar.setPadding(0, 0, 0, 0)
        }

        checkNotchSupport()
        showSystemUI(true)
        val bundle = Bundle()
        val file = File(mUri.toString())
        val intentType = intent.type ?: ""
        val type = when {
            filename.isVideoFast() || intentType.startsWith("video/") -> TYPE_VIDEOS
            else -> TYPE_AUDIOS
        }

        mIsVideo = (type == TYPE_VIDEOS || type == TYPE_AUDIOS)
        mMedium = Medium(null, filename, mUri.toString(), mUri!!.path!!.getParentPath(), 0, 0, file.length(), type, 0,"", false, 0L, 0)
        binding.fragmentViewerToolbar.title = Html.fromHtml("<font color='${Color.WHITE.toHex()}'>${mMedium!!.name}</font>")
        bundle.putSerializable(MEDIUM, mMedium)

        if (savedInstanceState == null) {
            mFragment = if (mIsVideo) VideoFragment() else null
            mFragment!!.listener = this
            mFragment!!.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.fragment_placeholder, mFragment!!).commit()
        }

        if (config.blackBackground) {
            binding.fragmentHolder.background = ColorDrawable(Color.BLACK)
        }

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val isFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            mFragment?.fullscreenToggled(isFullscreen)
        }

        initBottomActions()
    }

    private fun launchVideoPlayer() {
        val newUri = getFinalUriFromPath(mUri.toString(), BuildConfig.APPLICATION_ID)
        if (newUri == null) {
            toast(org.skywaves.mediavox.core.R.string.unknown_error_occurred)
            return
        }

        hideKeyboard()
        val mimeType = getUriMimeType(mUri.toString(), newUri)
        Intent(applicationContext, VideoPlayerActivity::class.java).apply {
            setDataAndType(newUri, mimeType)
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            if (intent.extras != null) {
                putExtras(intent.extras!!)
            }

            startActivity(this)
        }

        finish()
    }

    private fun sendViewPagerIntent(path: String) {
        ensureBackgroundThread {
            if (isPathPresentInMediaStore(path)) {
                openViewPager(path)
            } else {
                rescanPath(path) {
                    openViewPager(path)
                }
            }
        }
    }

    private fun openViewPager(path: String) {
        if (!intent.getBooleanExtra(IS_FROM_GALLERY, false)) {
            MediaActivity.mMedia.clear()
        }
        runOnUiThread {
            hideKeyboard()
            Intent(this, ViewPagerActivity::class.java).apply {
                putExtra(SKIP_AUTHENTICATION, intent.getBooleanExtra(SKIP_AUTHENTICATION, false))
                putExtra(SHOW_FAVORITES, intent.getBooleanExtra(SHOW_FAVORITES, false))
                putExtra(IS_VIEW_INTENT, true)
                putExtra(IS_FROM_GALLERY, mIsFromGallery)
                putExtra(PATH, path)
                startActivity(this)
            }
        }
    }

    private fun isPathPresentInMediaStore(path: String): Boolean {
        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Video.Media.DATA} = ?"
        val selectionArgs = arrayOf(path)

        try {
            val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)
            cursor?.use {
                return cursor.moveToFirst()
            }
        } catch (e: Exception) {
        }

        return false
    }

    private fun showProperties() {
        PropertiesDialog(this, mUri!!.path!!)
    }

    private fun isFileTypeVisible(path: String): Boolean {
        val filter = config.filterMedia
        return !(path.isVideoFast() && filter and TYPE_VIDEOS == 0 ||
            path.isAudioFast() && filter and TYPE_AUDIOS == 0)
    }

    private fun initBottomActions() {
        initBottomActionButtons()
        initBottomActionsLayout()
    }

    private fun initBottomActionsLayout() {
        binding.bottomActions.root.layoutParams.height = resources.getDimension(R.dimen.bottom_actions_height).toInt() + navigationBarHeight
        if (config.bottomActions) {
            binding.bottomActions.root.beVisible()
        } else {
            binding.bottomActions.root.beGone()
        }
    }

    private fun initBottomActionButtons() {
        arrayListOf(
            binding.bottomActions.bottomFavorite,
            binding.bottomActions.bottomDelete,
            binding.bottomActions.bottomProperties,
            binding.bottomActions.bottomChangeOrientation,
            binding.bottomActions.bottomSlideshow,
            binding.bottomActions.bottomToggleFileVisibility
        ).forEach {
            it.beGone()
        }

        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0

        binding.bottomActions.bottomShare.beVisibleIf(visibleBottomActions and BOTTOM_ACTION_SHARE != 0)
        binding.bottomActions.bottomShare.setOnClickListener {
            if (mUri != null && binding.bottomActions.root.alpha == 1f) {
                sharePath(mUri!!.toString())
            }
        }
    }

    override fun fragmentClicked() {
        mIsFullScreen = !mIsFullScreen
        if (mIsFullScreen) {
            hideSystemUI(true)
        } else {
            showSystemUI(true)
        }

        val newAlpha = if (mIsFullScreen) 0f else 1f
        binding.topShadow.animate().alpha(newAlpha).start()
        if (!binding.bottomActions.root.isGone()) {
            binding.bottomActions.root.animate().alpha(newAlpha).start()
        }

        binding.fragmentViewerToolbar.animate().alpha(newAlpha).withStartAction {
            binding.fragmentViewerToolbar.beVisible()
        }.withEndAction {
            binding.fragmentViewerToolbar.beVisibleIf(newAlpha == 1f)
        }.start()
    }

    override fun videoEnded() = false

    override fun goToPrevItem() {}

    override fun goToNextItem() {}

    override fun launchViewVideoIntent(path: String) {}

    override fun isSlideShowActive() = false
}
