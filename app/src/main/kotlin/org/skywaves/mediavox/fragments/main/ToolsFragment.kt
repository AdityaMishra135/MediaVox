package org.skywaves.mediavox.fragments.main

import android.content.Context.STORAGE_SERVICE
import android.content.res.ColorStateList
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.skywaves.mediavox.R
import org.skywaves.mediavox.activities.MainActivity
import org.skywaves.mediavox.activities.SettingsActivity
import org.skywaves.mediavox.core.extensions.adjustAlpha
import org.skywaves.mediavox.core.extensions.beGone
import org.skywaves.mediavox.core.extensions.beVisible
import org.skywaves.mediavox.core.extensions.formatSizeThousand
import org.skywaves.mediavox.core.extensions.getLongValue
import org.skywaves.mediavox.core.extensions.getProperBackgroundColor
import org.skywaves.mediavox.core.extensions.getProperPrimaryColor
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.getStringValue
import org.skywaves.mediavox.core.extensions.queryCursor
import org.skywaves.mediavox.core.helpers.isNougatPlus
import org.skywaves.mediavox.core.helpers.isOreoPlus
import org.skywaves.mediavox.core.views.CircleProgressBar
import org.skywaves.mediavox.databinding.DialogStorageInfoBinding
import org.skywaves.mediavox.databinding.FragmentSettingsGeneralBinding
import org.skywaves.mediavox.databinding.FragmentToolsBinding
import org.skywaves.mediavox.fragments.settings.base.SettingsBaseFragment
import org.skywaves.mediavox.helpers.AUDIO
import org.skywaves.mediavox.helpers.PRIMARY_VOLUME_NAME
import org.skywaves.mediavox.helpers.VIDEOS
import org.skywaves.mediavox.helpers.extraAudioMimeTypes
import android.app.Activity
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Context.STORAGE_SERVICE
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.skywaves.mediavox.activities.MediaActivity
import org.skywaves.mediavox.activities.SimpleActivity
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.ConfirmationDialog
import org.skywaves.mediavox.core.dialogs.FolderLockingNoticeDialog
import org.skywaves.mediavox.core.dialogs.SecurityDialog
import org.skywaves.mediavox.core.extensions.convertToBitmap
import org.skywaves.mediavox.core.extensions.getFilenameFromPath
import org.skywaves.mediavox.core.extensions.handleDeletePasswordProtection
import org.skywaves.mediavox.core.extensions.handleLockedFolderOpening
import org.skywaves.mediavox.core.helpers.FAVORITES
import org.skywaves.mediavox.core.helpers.SHOW_ALL_TABS
import org.skywaves.mediavox.core.helpers.ensureBackgroundThread
import org.skywaves.mediavox.dialogs.ConfirmDeleteFolderDialog
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.directoryDB
import org.skywaves.mediavox.extensions.emptyAndDisableTheRecycleBin
import org.skywaves.mediavox.extensions.emptyTheRecycleBin
import org.skywaves.mediavox.extensions.favoritesDB
import org.skywaves.mediavox.extensions.getShortcutImage
import org.skywaves.mediavox.extensions.mediaDB
import org.skywaves.mediavox.extensions.openRecycleBin
import org.skywaves.mediavox.extensions.showRecycleBinEmptyingDialog
import org.skywaves.mediavox.helpers.DIRECTORY
import org.skywaves.mediavox.helpers.RECYCLE_BIN
import org.skywaves.mediavox.helpers.SKIP_AUTHENTICATION
import java.io.File
import java.util.Locale

class ToolsFragment : Fragment() {
    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!
    private val SIZE_DIVIDER = 100000
    private var lockedFolderPaths = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolsItemsHolder.backgroundTintList = ColorStateList.valueOf(requireContext().getProperBackgroundColor())
        binding.toolsHolder.setBackgroundColor(requireContext().getProperPrimaryColor().adjustAlpha(.6f))
        binding.backPressed.backgroundTintList = ColorStateList.valueOf(requireContext().getProperBackgroundColor())
        binding.backPressed.setOnClickListener {
            handleBackPressed()
        }
        getVolumeStorageStats()
        lifecycleScope.launch {
            getSizes(null, null.toString())
        }


        arrayOf(binding.storageTotalSize, binding.storageUsedSize, binding.storageFreeSize, binding.storageExternalTotalSize, binding.storageExternalUsedSize, binding.storageExternalFreeSize, binding.storageAnalydeText).forEach {
            it.setTextColor(requireContext().getProperTextColor())
        }
        arrayOf(binding.totolVideosSize, binding.totolAudiosSize, binding.totolOthersSize, binding.totolExternalVideosSize, binding.totolExternalAudiosSize, binding.totolExternalOthersSize).forEach {
            it.setTextColor(requireContext().getProperTextColor())
        }
        binding.mainStorageExternalUsageProgressbar.setProgressColor(requireContext().getProperPrimaryColor())
        binding.mainStorageUsageProgressbar.setProgressColor(requireContext().getProperPrimaryColor())
        binding.mainStorageUsageProgressbar.setBackgroundColor(requireContext().getProperPrimaryColor().adjustAlpha(.5f))
        binding.mainStorageExternalUsageProgressbar.setBackgroundColor(requireContext().getProperPrimaryColor().adjustAlpha(.5f))
        binding.favoriteTools.setOnClickListener {
            requireActivity().handleLockedFolderOpening(FAVORITES) { success ->
                if (success) {
                    val intent = Intent(requireContext(), MediaActivity::class.java).apply {
                        putExtra(SKIP_AUTHENTICATION, true)
                        putExtra(DIRECTORY, FAVORITES)
                    }
                    requireContext().startActivity(intent)
        }
        }
        }

        binding.recycleBinTools.setOnClickListener {
            requireActivity().handleLockedFolderOpening(RECYCLE_BIN) { success ->
                if (success) {
                    val intent = Intent(requireContext(), MediaActivity::class.java).apply {
                        putExtra(SKIP_AUTHENTICATION, true)
                        putExtra(DIRECTORY, RECYCLE_BIN)
                    }
                    requireContext().startActivity(intent)
        }
            }
        }
        binding.favoriteTools.setOnLongClickListener {
            showPopupFav(it)
            true
        }
        binding.recycleBinTools.setOnLongClickListener {
            showPopupRecycle(it)
    // Handle long click event here
    true // Return true to indicate the event was consumed
        }
    }

    companion object {
        val TAG: String = ToolsFragment::class.java.simpleName

        val instance: ToolsFragment
            get() = ToolsFragment()
    }

    fun handleBackPressed() {
        (requireActivity() as MainActivity).mFragmentManager!!.beginTransaction().remove(this).commit()
        (requireActivity() as MainActivity).updateStatusbarColor(requireContext().getProperBackgroundColor())
        (requireActivity() as MainActivity).binding.mainContentContainer.beGone()
        (requireActivity() as MainActivity).toolShow = false
        arrayOf(
            (requireActivity() as MainActivity).binding.xyz,
            (requireActivity() as MainActivity).binding.mainMenu,
            (requireActivity() as MainActivity).binding.directoriesHolder,
            (requireActivity() as MainActivity).binding.lastPlayed
        ).forEach {
            it.beVisible()
        }
    }

    private fun showPopupFav(view: View) {
        val popup = PopupMenu(requireActivity(), view)

        // Add menu items programmatically
        popup.menu.add(0, 1, 0, "Lock Favorite")
        popup.menu.add(0, 2, 1, "Clear Favorite")
        popup.menu.add(0, 3, 2, "Unlock Favorite")
        popup.menu.findItem(1).isVisible = FAVORITES.any { !requireContext().config.isFolderProtected(FAVORITES) }
        popup.menu.findItem(3).isVisible = FAVORITES.any { requireContext().config.isFolderProtected(FAVORITES) }
        popup.menu.findItem(2).isVisible = requireContext().config.favCount > 0

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> {
                    tryLockFav()
                    true
                }
                2 -> {
                    askConfirmDeleteFav()
                        Toast.makeText(requireContext(),requireContext().config.trashItemCount.toInt(),Toast.LENGTH_LONG).show()
                    true
                }
                3 -> {
                    unlockFav()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showPopupRecycle(view: View) {
        val popup = PopupMenu(requireActivity(), view)
        // Add menu items programmatically
        popup.menu.add(0, 1, 0, "Lock Trash")
        popup.menu.add(0, 2, 1, "Clean Trash")
        popup.menu.add(0, 3, 2, "Unlock Trash")
        popup.menu.findItem(1).isVisible = RECYCLE_BIN.any { !requireContext().config.isFolderProtected(RECYCLE_BIN) }
        popup.menu.findItem(3).isVisible = RECYCLE_BIN.any { requireContext().config.isFolderProtected(RECYCLE_BIN) }
        popup.menu.findItem(2).isVisible = requireContext().config.trashItemCount > 0

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> {
                    tryLockTrashBin()
                    true
                }
                2 -> {
                    askConfirmDelete()
                    true
                }
                3 -> {
                    unlockTrashBin()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun tryLockFav() {
        if (requireContext().config.wasFolderLockingNoticeShown) {
            lockFav()
        } else {
            FolderLockingNoticeDialog(requireActivity()) {
                lockFav()
            }
        }
    }

private fun tryLockTrashBin() {
        if (requireContext().config.wasFolderLockingNoticeShown) {
            lockTrashBin()
        } else {
            FolderLockingNoticeDialog(requireActivity()) {
                lockTrashBin()
            }
        }
    }
    
    private fun lockFav() {
        SecurityDialog(requireActivity(), "", SHOW_ALL_TABS) { hash, type, success ->
            if (success) {
                FAVORITES.filter { !requireContext().config.isFolderProtected(FAVORITES) }.forEach {
                    requireContext().config.addFolderProtection(FAVORITES, hash, type)
                    lockedFolderPaths.add(FAVORITES)
                }
              
            }
        }
    }

    private fun lockTrashBin() {
        SecurityDialog(requireActivity(), "", SHOW_ALL_TABS) { hash, type, success ->
            if (success) {
                RECYCLE_BIN.filter { !requireContext().config.isFolderProtected(RECYCLE_BIN) }.forEach {
                    requireContext().config.addFolderProtection(RECYCLE_BIN, hash, type)
                    lockedFolderPaths.add(RECYCLE_BIN)
                }
              
            }
        }
    }

    private fun unlockFav() {
        val paths = FAVORITES
        val firstPath = paths
        val tabToShow = requireContext().config.getFolderProtectionType(firstPath)
        val hashToCheck = requireContext().config.getFolderProtectionHash(firstPath)
        SecurityDialog(requireActivity(), hashToCheck, tabToShow) { hash, type, success ->
            if (success) {
                paths.filter { requireContext().config.isFolderProtected(paths) && requireContext().config.getFolderProtectionType(paths) == tabToShow && requireContext().config.getFolderProtectionHash(paths) == hashToCheck }
                    .forEach {
                        requireContext().config.removeFolderProtection(paths)
                        lockedFolderPaths.remove(paths)
                    }
            }
        }
    }

    
private fun unlockTrashBin() {
        val paths = RECYCLE_BIN
        val firstPath = paths
        val tabToShow = requireContext().config.getFolderProtectionType(firstPath)
        val hashToCheck = requireContext().config.getFolderProtectionHash(firstPath)
        SecurityDialog(requireActivity(), hashToCheck, tabToShow) { hash, type, success ->
            if (success) {
                paths.filter { requireContext().config.isFolderProtected(paths) && requireContext().config.getFolderProtectionType(paths) == tabToShow && requireContext().config.getFolderProtectionHash(paths) == hashToCheck }
                    .forEach {
                        requireContext().config.removeFolderProtection(paths)
                        lockedFolderPaths.remove(paths)
                    }
            }
        }
    }

    private fun tryCreateFavShortcut() {
        if (!isOreoPlus()) {
            return
        }
        requireActivity().handleLockedFolderOpening(FAVORITES) { success ->
            if (success) {
                createShortcutFav()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createShortcutFav() {
        val manager = requireActivity().getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported) {
            val path = FAVORITES
            val drawable = resources.getDrawable(R.drawable.shortcut_image).mutate()
            val coverThumbnail = requireContext().directoryDB.getDirectoryThumbnail(path)
            requireActivity().getShortcutImage(coverThumbnail.toString(), drawable) {
                val intent = Intent(requireActivity(), MediaActivity::class.java)
                intent.action = Intent.ACTION_VIEW
                intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(DIRECTORY, path)
               val label =  "Favorite"
                val shortcut = ShortcutInfo.Builder(requireActivity(), path)
                    .setShortLabel(label)
                    .setIcon(Icon.createWithBitmap(drawable.convertToBitmap()))
                    .setIntent(intent)
                    .build()

                manager.requestPinShortcut(shortcut, null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getVolumeStorageStats() {
        lifecycleScope.launch {
            val storageStats = withContext(Dispatchers.IO) {
                fetchStorageStats()
            }
            updateUI(storageStats)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchStorageStats(): List<StorageStat> {
        val externalDirs = requireContext().getExternalFilesDirs(null)
        val storageManager = requireContext().applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
        val storageStats = mutableListOf<StorageStat>()

        externalDirs.forEach { file ->
            val storageVolume = storageManager.getStorageVolume(file) ?: return@forEach
            val (volumeName, totalStorageSpace, freeStorageSpace) = getStorageStats(storageVolume, file)
            val usedStorageSpace = totalStorageSpace - freeStorageSpace

            storageStats.add(StorageStat(volumeName, totalStorageSpace, freeStorageSpace, usedStorageSpace))
        }
        return storageStats
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getStorageStats(storageVolume: StorageVolume, file: File): Triple<String, Long, Long> {
        val totalStorageSpace: Long
        val freeStorageSpace: Long
        val volumeName: String = if (storageVolume.isPrimary) {
            PRIMARY_VOLUME_NAME
        } else {
            storageVolume.uuid!!.lowercase(Locale.US)
        }

        if (isOreoPlus() && storageVolume.isPrimary) {
            val storageStatsManager = requireContext().getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
            val uuid = StorageManager.UUID_DEFAULT
            totalStorageSpace = storageStatsManager.getTotalBytes(uuid)
            freeStorageSpace = storageStatsManager.getFreeBytes(uuid)
        } else {
            totalStorageSpace = file.totalSpace
            freeStorageSpace = file.freeSpace
        }

        return Triple(volumeName, totalStorageSpace, freeStorageSpace)
    }

    private suspend fun updateUI(storageStats: List<StorageStat>) {
        val totalSizeHolder = binding.storageTotalSize
        val usedSizeHolder = binding.storageUsedSize
        val freeSizeHolder = binding.storageFreeSize
        val storageProgressView = binding.mainStorageUsageProgressbar
        val totalExternalSizeHolder = binding.storageExternalTotalSize
        val usedExternalSizeHolder = binding.storageExternalUsedSize
        val freeExternalSizeHolder = binding.storageExternalFreeSize
        val storageExternalProgressView = binding.mainStorageExternalUsageProgressbar


        storageStats.forEach { stat ->
            if (stat.volumeName == PRIMARY_VOLUME_NAME) {
                updateUI(storageProgressView, totalSizeHolder, usedSizeHolder, freeSizeHolder, stat.totalSpace, stat.freeSpace, stat.usedSpace)
            } else {
                updateUI(storageExternalProgressView, totalExternalSizeHolder, usedExternalSizeHolder, freeExternalSizeHolder, stat.totalSpace, stat.freeSpace, stat.usedSpace)
            }

            lifecycleScope.launch {
                getSizes(stat.usedSpace, stat.volumeName)
            }
        }
    }

    private fun updateUI(
        progressView: CircleProgressBar, totalHolder: TextView, usedHolder: TextView, freeHolder: TextView,
        totalSpace: Long, freeSpace: Long, usedSpace: Long
    ) {
        progressView.maxValue = (totalSpace / SIZE_DIVIDER).toFloat()
        progressView.text = "${freeSpace.formatSizeThousand()}/${totalSpace.formatSizeThousand()}"
        progressView.progress = ((totalSpace - freeSpace) / SIZE_DIVIDER).toFloat()
        progressView.beVisible()
        freeHolder.text = "Free: ${freeSpace.formatSizeThousand()}"
        totalHolder.text = "Total: ${totalSpace.formatSizeThousand()}"
        usedHolder.text = "Used: ${usedSpace.formatSizeThousand()}"
    }

    private suspend fun getSizes(usedStorageSpace: Long?, volumeName: String) {
        if (!isOreoPlus()) return

        if (volumeName == PRIMARY_VOLUME_NAME) {
            binding.mainStorageUsageProgressbar.text = "Internal"
        } else {
            binding.mainStorageExternalUsageProgressbar.text = "SD Card"
        }

        val filesSize = withContext(Dispatchers.IO) { getSizesByMimeType(volumeName) }
        val fileSizeVideos = filesSize[VIDEOS]!!
        val fileSizeAudios = filesSize[AUDIO]!!

        if (volumeName == PRIMARY_VOLUME_NAME) {
            binding.totolVideosSize.text = "Videos: ${fileSizeVideos.formatSizeThousand()}"
            binding.totolAudiosSize.text = "Audios: ${fileSizeAudios.formatSizeThousand()}"
            if (usedStorageSpace != null) {
                binding.totolOthersSize.text = "Others: ${(usedStorageSpace - (fileSizeAudios + fileSizeVideos)).formatSizeThousand()}"
            }
        } else {
            binding.totolExternalVideosSize.text = "Videos: ${fileSizeVideos.formatSizeThousand()}"
            binding.totolExternalAudiosSize.text = "Audios: ${fileSizeAudios.formatSizeThousand()}"
            if (usedStorageSpace != null) {
                binding.totolExternalOthersSize.text = "Others: ${(usedStorageSpace - (fileSizeAudios + fileSizeVideos)).formatSizeThousand()}"
            }
        }
    }

    private fun getSizesByMimeType(volumeName: String): HashMap<String, Long> {
        val uri = MediaStore.Files.getContentUri(volumeName)
        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        var videosSize = 0L
        var audioSize = 0L
        requireContext().applicationContext.queryCursor(uri, projection) { cursor ->
            val mimeType = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)?.lowercase(Locale.getDefault())
            val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
            if (mimeType != null) {
                when (mimeType.substringBefore("/")) {
                    "video" -> videosSize += size
                    "audio" -> audioSize += size
                    else -> {
                        if (extraAudioMimeTypes.contains(mimeType)) audioSize += size
                    }
                }
            }
        }

        return hashMapOf(
            VIDEOS to videosSize,
            AUDIO to audioSize
        )
    }

    fun getAllVolumeNames(): List<String> {
        val volumeNames = mutableListOf(PRIMARY_VOLUME_NAME)
        if (isNougatPlus()) {
            val storageManager = requireContext().applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
            requireContext().getExternalFilesDirs(null)
                .mapNotNull { storageManager.getStorageVolume(it) }
                .filterNot { it.isPrimary }
                .mapNotNull { it.uuid?.lowercase(Locale.US) }
                .forEach { volumeNames.add(it) }
        }
        return volumeNames
    }

    data class StorageStat(
        val volumeName: String,
        val totalSpace: Long,
        val freeSpace: Long,
        val usedSpace: Long
    )


    private fun askConfirmDelete() {
        when {
            requireContext().config.isDeletePasswordProtectionOn -> requireActivity().handleDeletePasswordProtection {
                deleteFolders()
            }

            requireContext().config.skipDeleteConfirmation -> deleteFolders()
            else -> {
                    ConfirmationDialog(
                        requireActivity(),
                        "",
                        org.skywaves.mediavox.core.R.string.empty_recycle_bin_confirmation,
                        org.skywaves.mediavox.core.R.string.yes,
                        org.skywaves.mediavox.core.R.string.no
                    ) {
                        deleteFolders()
                    }
                    return
                }
        }
    }

    private fun deleteFolders() {
        val SAFPath = RECYCLE_BIN
        (requireActivity() as BaseSimpleActivity).handleSAFDialog(SAFPath) {
            if (!it) {
                return@handleSAFDialog
            }

            (requireActivity() as BaseSimpleActivity).handleSAFDialogSdk30(SAFPath) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }
                tryEmptyRecycleBin(false)
            }
        }
    }


    private fun tryEmptyRecycleBin(askConfirmation: Boolean) {
        if (askConfirmation) {
            (requireActivity() as BaseSimpleActivity).showRecycleBinEmptyingDialog {
                emptyRecycleBin()
            }
        } else {
            emptyRecycleBin()
        }
    }

    private fun emptyRecycleBin() {
        requireActivity().handleLockedFolderOpening(RECYCLE_BIN) { success ->
            if (success) {
                (requireActivity() as BaseSimpleActivity).emptyTheRecycleBin {
                }
            }
        }
    }

    private fun askConfirmDeleteFav() {
        when {
            requireContext().config.isDeletePasswordProtectionOn -> requireActivity().handleDeletePasswordProtection {
                deleteFoldersFav()
            }

            requireContext().config.skipDeleteConfirmation -> deleteFolders()
            else -> {

                    ConfirmationDialog(
                        requireActivity(),
                        "Are you sure you want to Clear Favorite?",
                        org.skywaves.mediavox.core.R.string.yes,
                        org.skywaves.mediavox.core.R.string.no
                    ) {
                        deleteFoldersFav()
                    }
                    return
                }

        }
    }

    private fun deleteFoldersFav() {

        val SAFPath = FAVORITES
        (requireActivity() as BaseSimpleActivity).handleSAFDialog(SAFPath) {
            if (!it) {
                return@handleSAFDialog
            }

            (requireActivity() as BaseSimpleActivity).handleSAFDialogSdk30(SAFPath) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }


                requireActivity().handleLockedFolderOpening(FAVORITES) { success ->
                    if (success) {
                        ensureBackgroundThread {
                            requireActivity().mediaDB.clearFavorites()
                            requireActivity().favoritesDB.clearFavorites()
                        }
                    }
                }

            }

        }
    }

}
