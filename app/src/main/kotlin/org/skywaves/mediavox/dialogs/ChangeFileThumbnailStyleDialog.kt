package org.skywaves.mediavox.dialogs

import android.content.DialogInterface
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.RadioGroupDialog
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.core.models.RadioItem
import org.skywaves.mediavox.databinding.DialogChangeFileThumbnailStyleBinding
import org.skywaves.mediavox.extensions.config

class ChangeFileThumbnailStyleDialog(val activity: BaseSimpleActivity) : DialogInterface.OnClickListener {
    private var config = activity.config
    private val binding: DialogChangeFileThumbnailStyleBinding

    init {
        binding = DialogChangeFileThumbnailStyleBinding.inflate(activity.layoutInflater).apply {
            dialogFileStyleShowThumbnailFileTypes.isChecked = config.showThumbnailFileTypes
            dialogFileStyleShowThumbnailFileDir.isChecked = config.showThumbnailFileDir
            dialogFileStyleShowMediumFileSize.isChecked = config.showMediumSize

            dialogFileStyleShowThumbnailFileTypesHolder.setOnClickListener { dialogFileStyleShowThumbnailFileTypes.toggle() }
            dialogFileStyleShowfileDirsHolder.setOnClickListener { dialogFileStyleShowThumbnailFileDir.toggle() }
            dialogFileStyleShowMediumSizeHolder.setOnClickListener { dialogFileStyleShowMediumFileSize.toggle() }

        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok, this)
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        config.showThumbnailFileTypes = binding.dialogFileStyleShowThumbnailFileTypes.isChecked
        config.showThumbnailFileDir = binding.dialogFileStyleShowThumbnailFileDir.isChecked
        config.showMediumSize = binding.dialogFileStyleShowMediumFileSize.isChecked
    }

}
