package org.skywaves.mediavox.dialogs

import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.beVisibleIf
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.core.helpers.VIEW_TYPE_GRID
import org.skywaves.mediavox.core.helpers.VIEW_TYPE_LIST
import org.skywaves.mediavox.databinding.DialogChangeViewTypeBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.helpers.SHOW_ALL

class ChangeViewTypeDialog(val activity: BaseSimpleActivity, val fromFoldersView: Boolean, val path: String = "", val callback: () -> Unit) {
    private val binding = DialogChangeViewTypeBinding.inflate(activity.layoutInflater)
    private var config = activity.config
    private var pathToUse = if (path.isEmpty()) SHOW_ALL else path

    init {
        binding.apply {
            val viewToCheck = if (fromFoldersView) {
                if (config.viewTypeFolders == VIEW_TYPE_GRID) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            } else {
                val currViewType = config.getFolderViewType(pathToUse)
                if (currViewType == VIEW_TYPE_GRID) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            }

            changeViewTypeDialogRadio.check(viewToCheck)
            changeViewTypeDialogGroupDirectSubfolders.apply {
                beVisibleIf(fromFoldersView)
                isChecked = config.groupDirectSubfolders
            }

            changeViewTypeDialogUseForThisFolder.apply {
                beVisibleIf(!fromFoldersView)
                isChecked = config.hasCustomViewType(pathToUse)
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val viewType = if (binding.changeViewTypeDialogRadio.checkedRadioButtonId == binding.changeViewTypeDialogRadioGrid.id) {
            VIEW_TYPE_GRID
        } else {
            VIEW_TYPE_LIST
        }

        if (fromFoldersView) {
            config.viewTypeFolders = viewType
            config.groupDirectSubfolders = binding.changeViewTypeDialogGroupDirectSubfolders.isChecked
        } else {
            if (binding.changeViewTypeDialogUseForThisFolder.isChecked) {
                config.saveFolderViewType(pathToUse, viewType)
            } else {
                config.removeFolderViewType(pathToUse)
                config.viewTypeFiles = viewType
            }
        }


        callback()
    }
}
