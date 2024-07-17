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
import org.skywaves.mediavox.core.helpers.FAVORITES
import org.skywaves.mediavox.extensions.openRecycleBin
import org.skywaves.mediavox.helpers.DIRECTORY
import org.skywaves.mediavox.helpers.RECYCLE_BIN
import java.io.File
import java.util.Locale

class ToolsFragment : Fragment() {
    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!
    private val SIZE_DIVIDER = 100000

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
            val intent = Intent(requireContext(), MediaActivity::class.java).apply {
                putExtra(DIRECTORY, FAVORITES)
            }
            requireContext().startActivity(intent)

        }
        binding.recycleBinTools.setOnClickListener {
            val intent = Intent(requireContext(), MediaActivity::class.java).apply {
                putExtra(DIRECTORY, RECYCLE_BIN)
            }
            requireContext().startActivity(intent)
        }
        binding.favMenu.setOnClickListener {view ->
            showPopupFav(view)
        }
        binding.recycleMenu.setOnClickListener {view ->
            showPopupRecycle(view)
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
        popup.menu.add(0, 2, 1, "Create Shortcut")

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> {
                    Toast.makeText(requireContext(), "Lock Favorite", Toast.LENGTH_SHORT).show()
                    true
                }
                2 -> {
                    Toast.makeText(requireContext(), "Create Shortcut", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showPopupRecycle(view: View) {
            Toast.makeText(requireContext(), "Lock Favorite", Toast.LENGTH_SHORT).show()
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
}
