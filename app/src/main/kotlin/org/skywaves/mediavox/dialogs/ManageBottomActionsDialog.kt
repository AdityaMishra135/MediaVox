package org.skywaves.mediavox.dialogs

import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.databinding.DialogManageBottomActionsBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.helpers.*

class ManageBottomActionsDialog(val activity: BaseSimpleActivity, val callback: (result: Int) -> Unit) {
    private val binding = DialogManageBottomActionsBinding.inflate(activity.layoutInflater)

    init {
        val actions = activity.config.visibleBottomActions
        binding.apply {
            manageBottomActionsToggleFavorite.isChecked = actions and BOTTOM_ACTION_TOGGLE_FAVORITE != 0
            manageBottomActionsShare.isChecked = actions and BOTTOM_ACTION_SHARE != 0
            manageBottomActionsDelete.isChecked = actions and BOTTOM_ACTION_DELETE != 0
            manageBottomActionsProperties.isChecked = actions and BOTTOM_ACTION_PROPERTIES != 0
            manageBottomActionsChangeOrientation.isChecked = actions and BOTTOM_ACTION_CHANGE_ORIENTATION != 0
            manageBottomActionsSlideshow.isChecked = actions and BOTTOM_ACTION_SLIDESHOW != 0
            manageBottomActionsToggleVisibility.isChecked = actions and BOTTOM_ACTION_TOGGLE_VISIBILITY != 0
            manageBottomActionsRename.isChecked = actions and BOTTOM_ACTION_RENAME != 0
            manageBottomActionsCopy.isChecked = actions and BOTTOM_ACTION_COPY != 0
            manageBottomActionsMove.isChecked = actions and BOTTOM_ACTION_MOVE != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok) { dialog, which -> dialogConfirmed() }
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        binding.apply {
            if (manageBottomActionsToggleFavorite.isChecked)
                result += BOTTOM_ACTION_TOGGLE_FAVORITE
            if (manageBottomActionsShare.isChecked)
                result += BOTTOM_ACTION_SHARE
            if (manageBottomActionsDelete.isChecked)
                result += BOTTOM_ACTION_DELETE
            if (manageBottomActionsProperties.isChecked)
                result += BOTTOM_ACTION_PROPERTIES
            if (manageBottomActionsChangeOrientation.isChecked)
                result += BOTTOM_ACTION_CHANGE_ORIENTATION
            if (manageBottomActionsSlideshow.isChecked)
                result += BOTTOM_ACTION_SLIDESHOW
            if (manageBottomActionsToggleVisibility.isChecked)
                result += BOTTOM_ACTION_TOGGLE_VISIBILITY
            if (manageBottomActionsRename.isChecked)
                result += BOTTOM_ACTION_RENAME
            if (manageBottomActionsCopy.isChecked)
                result += BOTTOM_ACTION_COPY
            if (manageBottomActionsMove.isChecked)
                result += BOTTOM_ACTION_MOVE
        }

        activity.config.visibleBottomActions = result
        callback(result)
    }
}
