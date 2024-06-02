package org.skywaves.mediavox.dialogs

import android.app.Activity
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Context.STORAGE_SERVICE
import android.os.Build
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.beVisible
import org.skywaves.mediavox.core.extensions.formatSizeThousand
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.getLongValue
import org.skywaves.mediavox.core.extensions.getProperTextColor
import org.skywaves.mediavox.core.extensions.getStringValue
import org.skywaves.mediavox.core.extensions.queryCursor
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.core.helpers.isNougatPlus
import org.skywaves.mediavox.core.helpers.isOreoPlus
import org.skywaves.mediavox.databinding.DialogStorageInfoBinding
import org.skywaves.mediavox.helpers.AUDIO
import org.skywaves.mediavox.helpers.PRIMARY_VOLUME_NAME
import org.skywaves.mediavox.helpers.VIDEOS
import org.skywaves.mediavox.helpers.extraAudioMimeTypes
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
class StorageInfoDialog(val activity: BaseSimpleActivity) {

    private val SIZE_DIVIDER = 100000

    init {
        val binding = DialogStorageInfoBinding.inflate(activity.layoutInflater)
        getVolumeStorageStats(binding)
        getSizes(binding,null)
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok) { dialog, which -> }
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog -> }
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getVolumeStorageStats(binding: DialogStorageInfoBinding) {
        val externalDirs = activity.getExternalFilesDirs(null)
        val storageManager = activity.applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager

        val totalSizeHolder = binding.storageTotalSize
        val usedSizeHolder = binding.storageUsedSize
        val freeSizeHolder = binding.storageFreeSize
        val storageProgressView = binding.mainStorageUsageProgressbar
        val totalExternalSizeHolder = binding.storageExternalTotalSize
        val usedExternalSizeHolder = binding.storageExternalUsedSize
        val freeExternalSizeHolder = binding.storageExternalFreeSize
        val storageExternalProgressView = binding.mainStorageExternalUsageProgressbar

        arrayOf(totalSizeHolder, usedSizeHolder, freeSizeHolder,totalExternalSizeHolder,usedExternalSizeHolder,freeExternalSizeHolder,binding.storageAnalydeText).forEach {
            it.setTextColor(activity.getProperTextColor())
        }


        externalDirs.forEach { file ->
            val volumeName: String
            val totalStorageSpace: Long
            val freeStorageSpace: Long
            val usedStorageSpace: Long
            val storageVolume = storageManager.getStorageVolume(file) ?: return
            if (storageVolume.isPrimary) {
                // internal storage
                volumeName = PRIMARY_VOLUME_NAME
                if (isOreoPlus()) {
                    val storageStatsManager = activity.applicationContext.getSystemService(Activity.STORAGE_STATS_SERVICE) as StorageStatsManager
                    val uuid = StorageManager.UUID_DEFAULT
                    totalStorageSpace = storageStatsManager.getTotalBytes(uuid)
                    freeStorageSpace = storageStatsManager.getFreeBytes(uuid)
                } else {
                    totalStorageSpace = file.totalSpace
                    freeStorageSpace = file.freeSpace
                }
                usedStorageSpace = totalStorageSpace - freeStorageSpace
            } else {
                volumeName = storageVolume.uuid!!.lowercase(Locale.US)
                totalStorageSpace = file.totalSpace
                freeStorageSpace = file.freeSpace
                usedStorageSpace = totalStorageSpace - freeStorageSpace
            }

            if (volumeName == PRIMARY_VOLUME_NAME) {
                storageProgressView.maxValue = (totalStorageSpace / SIZE_DIVIDER).toFloat()
                storageProgressView.text = "${freeStorageSpace.formatSizeThousand()}/${totalStorageSpace.formatSizeThousand()}"
                storageProgressView.progress = ((totalStorageSpace - freeStorageSpace) / SIZE_DIVIDER).toFloat()
                storageProgressView.beVisible()
                freeSizeHolder.text  = "Free: ${freeStorageSpace.formatSizeThousand()}"
                totalSizeHolder.text = "Total: ${totalStorageSpace.formatSizeThousand()}"
                usedSizeHolder.text  = "Used: ${usedStorageSpace.formatSizeThousand()}"
                getSizes(binding,usedStorageSpace)
            } else {
                storageExternalProgressView.maxValue = (totalStorageSpace / SIZE_DIVIDER).toFloat()
                storageExternalProgressView.text = "${freeStorageSpace.formatSizeThousand()}/${totalStorageSpace.formatSizeThousand()}"
                storageExternalProgressView.progress = ((totalStorageSpace - freeStorageSpace) / SIZE_DIVIDER).toFloat()
                storageExternalProgressView.beVisible()
                freeExternalSizeHolder.text  = "Free: ${freeStorageSpace.formatSizeThousand()}"
                totalExternalSizeHolder.text = "Total: ${totalStorageSpace.formatSizeThousand()}"
                usedExternalSizeHolder.text  = "Used: ${usedStorageSpace.formatSizeThousand()}"
                getSizes(binding,usedStorageSpace)
            }
        }

    }



    private fun getSizes(binding: DialogStorageInfoBinding, usedStorageSpace: Long?) {
        if (!isOreoPlus()) {
            return
        }

        val totalVideosSizeHolder = binding.totolVideosSize
        val totalAudiosSizeHolder = binding.totolAudiosSize
        val totalOthersSizeHolder = binding.totolOthersSize
        val storageProgressView = binding.mainStorageUsageProgressbar
        val totalExternalVideosSizeHolder = binding.totolExternalVideosSize
        val totalExternalAudiosSizeHolder = binding.totolExternalAudiosSize
        val totalExternalOthersSizeHolder = binding.totolExternalOthersSize
        val storageExternalProgressView = binding.mainStorageExternalUsageProgressbar


        arrayOf(totalVideosSizeHolder, totalAudiosSizeHolder, totalOthersSizeHolder,totalExternalVideosSizeHolder,totalExternalAudiosSizeHolder,totalExternalOthersSizeHolder).forEach {
            it.setTextColor(activity.getProperTextColor())
        }
        val volumeNames = getAllVolumeNames()
        volumeNames.forEach { volumeName ->
            if (volumeName == PRIMARY_VOLUME_NAME) {
                storageProgressView.text = "Internal"
            } else { storageExternalProgressView.text = "SD Card" }

            val filesSize = getSizesByMimeType(volumeName)
            val fileSizeVideos = filesSize[VIDEOS]!!
            val fileSizeAudios = filesSize[AUDIO]!!

            if (volumeName == PRIMARY_VOLUME_NAME) {
                totalVideosSizeHolder.text = "Videos: ${fileSizeVideos.formatSizeThousand()}"
                totalExternalVideosSizeHolder.text = "Videos: ${fileSizeVideos.formatSizeThousand()}"
                totalAudiosSizeHolder.text = "Audios: ${fileSizeAudios.formatSizeThousand()}"
                totalExternalAudiosSizeHolder.text = "Audios: ${fileSizeAudios.formatSizeThousand()}"
                if (usedStorageSpace != null) {
                    totalOthersSizeHolder.text = "Others: ${(usedStorageSpace-(fileSizeAudios+fileSizeVideos)).formatSizeThousand()}"
                    totalExternalOthersSizeHolder.text = "Others: ${(usedStorageSpace-(fileSizeAudios+fileSizeVideos)).formatSizeThousand()}"
                }
            }
        }
    }

    private fun getSizesByMimeType(volumeName: String): HashMap<String, Long> {
        val uri = MediaStore.Files.getContentUri(volumeName)
        val projection = arrayOf(
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA
        )
        var videosSize = 0L
        var audioSize = 0L
        try {
            activity.applicationContext.queryCursor(uri, projection) { cursor ->
                try {
                    val mimeType = cursor.getStringValue(MediaStore.Files.FileColumns.MIME_TYPE)?.lowercase(Locale.getDefault())
                    val size = cursor.getLongValue(MediaStore.Files.FileColumns.SIZE)
                    if (mimeType != null) {
                        when (mimeType.substringBefore("/")) {
                            "video" -> videosSize += size
                            "audio" -> audioSize += size
                            else -> {
                                when {
                                    extraAudioMimeTypes.contains(mimeType) -> audioSize += size
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
        }

        val mimeTypeSizes = HashMap<String, Long>().apply {
            put(VIDEOS, videosSize)
            put(AUDIO, audioSize)
        }

        return mimeTypeSizes
    }

    fun getAllVolumeNames(): List<String> {
        val volumeNames = mutableListOf(PRIMARY_VOLUME_NAME)
        if (isNougatPlus()) {
            val storageManager = activity.applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
            activity.getExternalFilesDirs(null)
                .mapNotNull { storageManager.getStorageVolume(it) }
                .filterNot { it.isPrimary }
                .mapNotNull { it.uuid?.lowercase(Locale.US) }
                .forEach {
                    volumeNames.add(it)
                }
        }
        return volumeNames
    }
}
