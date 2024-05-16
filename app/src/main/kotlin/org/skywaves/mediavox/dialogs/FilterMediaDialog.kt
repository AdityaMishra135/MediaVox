package org.skywaves.mediavox.dialogs

import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.R
import org.skywaves.mediavox.databinding.DialogFilterMediaBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.helpers.*

class FilterMediaDialog(val activity: BaseSimpleActivity, val callback: (result: Int) -> Unit) {
    private val binding = DialogFilterMediaBinding.inflate(activity.layoutInflater)

    init {
        val filterMedia = activity.config.filterMedia
        binding.apply {
            filterMediaVideos.isChecked = filterMedia and TYPE_VIDEOS != 0
            filterMediaAudios.isChecked = filterMedia and TYPE_AUDIOS != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.filter_media)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        if (binding.filterMediaVideos.isChecked)
            result += TYPE_VIDEOS
        if (binding.filterMediaAudios.isChecked)
            result += TYPE_AUDIOS

        if (result == 0) {
            result = getDefaultFileFilter()
        }

        if (activity.config.filterMedia != result) {
            activity.config.filterMedia = result
            callback(result)
        }
    }
}
