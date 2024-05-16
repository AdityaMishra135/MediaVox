package org.skywaves.mediavox.dialogs

import android.content.DialogInterface
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.R
import org.skywaves.mediavox.adapters.toItemBinding
import org.skywaves.mediavox.databinding.DialogChangeFolderThumbnailStyleBinding
import org.skywaves.mediavox.databinding.DirectoryItemGridRoundedCornersBinding
import org.skywaves.mediavox.databinding.DirectoryItemGridSquareBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.helpers.*

class ChangeFolderThumbnailStyleDialog(val activity: BaseSimpleActivity, val callback: () -> Unit) : DialogInterface.OnClickListener {
    private var config = activity.config
    private val binding = DialogChangeFolderThumbnailStyleBinding.inflate(activity.layoutInflater).apply {
        dialogFolderLimitTitle.isChecked = config.limitFolderTitle
    }

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok, this)
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) {
                    setupStyle()
                    setupMediaCount()
                    updateSample()
                }
            }
    }

    private fun setupStyle() {
        val styleRadio = binding.dialogRadioFolderStyle
        styleRadio.setOnCheckedChangeListener { group, checkedId ->
            updateSample()
        }

        val styleBtn = when (config.folderStyle) {
            FOLDER_STYLE_SQUARE -> binding.dialogRadioFolderSquare
            else -> binding.dialogRadioFolderRoundedCorners
        }

        styleBtn.isChecked = true
    }

    private fun setupMediaCount() {
        val countRadio = binding.dialogRadioFolderCountHolder
        countRadio.setOnCheckedChangeListener { group, checkedId ->
            updateSample()
        }

        val countBtn = when (config.showFolderMediaCount) {
            FOLDER_MEDIA_CNT_LINE -> binding.dialogRadioFolderCountLine
            FOLDER_MEDIA_CNT_BRACKETS -> binding.dialogRadioFolderCountBrackets
            else -> binding.dialogRadioFolderCountNone
        }

        countBtn.isChecked = true
    }

    private fun updateSample() {
        val photoCount = 36
        val folderName = "Camera"
        binding.apply {
            val useRoundedCornersLayout = binding.dialogRadioFolderStyle.checkedRadioButtonId == R.id.dialog_radio_folder_rounded_corners
            binding.dialogFolderSampleHolder.removeAllViews()

            val sampleBinding = if (useRoundedCornersLayout) {
                DirectoryItemGridRoundedCornersBinding.inflate(activity.layoutInflater).toItemBinding()
            } else {
                DirectoryItemGridSquareBinding.inflate(activity.layoutInflater).toItemBinding()
            }
            val sampleView = sampleBinding.root
            binding.dialogFolderSampleHolder.addView(sampleView)

            sampleView.layoutParams.width = activity.resources.getDimension(R.dimen.sample_thumbnail_size).toInt()
            (sampleView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.CENTER_HORIZONTAL)

            when (binding.dialogRadioFolderCountHolder.checkedRadioButtonId) {
                R.id.dialog_radio_folder_count_line -> {
                    sampleBinding.dirName.text = folderName
                    sampleBinding.photoCnt.text = photoCount.toString()
                    sampleBinding.photoCnt.beVisible()
                }

                R.id.dialog_radio_folder_count_brackets -> {
                    sampleBinding.photoCnt.beGone()
                    sampleBinding.dirName.text = "$folderName ($photoCount)"
                }

                else -> {
                    sampleBinding.dirName.text = folderName
                    sampleBinding.photoCnt.beGone()
                }
            }

            val options = RequestOptions().centerCrop()
            var builder = Glide.with(activity)
                .load(R.drawable.sample_logo)
                .apply(options)

            if (useRoundedCornersLayout) {
                val cornerRadius = root.resources.getDimension(org.skywaves.mediavox.core.R.dimen.rounded_corner_radius_big).toInt()
                builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
                sampleBinding.dirName.setTextColor(activity.getProperTextColor())
                sampleBinding.photoCnt.setTextColor(activity.getProperTextColor())
            }

            builder.into(sampleBinding.dirThumbnail)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val style = when (binding.dialogRadioFolderStyle.checkedRadioButtonId) {
            R.id.dialog_radio_folder_square -> FOLDER_STYLE_SQUARE
            else -> FOLDER_STYLE_ROUNDED_CORNERS
        }

        val count = when (binding.dialogRadioFolderCountHolder.checkedRadioButtonId) {
            R.id.dialog_radio_folder_count_line -> FOLDER_MEDIA_CNT_LINE
            R.id.dialog_radio_folder_count_brackets -> FOLDER_MEDIA_CNT_BRACKETS
            else -> FOLDER_MEDIA_CNT_NONE
        }

        config.folderStyle = style
        config.showFolderMediaCount = count
        config.limitFolderTitle = binding.dialogFolderLimitTitle.isChecked
        callback()
    }
}
