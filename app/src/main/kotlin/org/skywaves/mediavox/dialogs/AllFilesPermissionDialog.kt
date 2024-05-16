package org.skywaves.mediavox.dialogs

import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.R

class AllFilesPermissionDialog(
    val activity: BaseSimpleActivity, message: String = "", val callback: (result: Boolean) -> Unit, val neutralPressed: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(org.skywaves.mediavox.core.R.layout.dialog_message, null)
        view.findViewById<TextView>(R.id.message).text = message

        activity.getAlertDialogBuilder().setPositiveButton(R.string.all_files) { dialog, which -> positivePressed() }
            .setNeutralButton(R.string.media_only) { dialog, which -> neutralPressed() }
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }
}
