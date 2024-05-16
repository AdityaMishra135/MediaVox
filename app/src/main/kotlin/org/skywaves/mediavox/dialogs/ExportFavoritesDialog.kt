package org.skywaves.mediavox.dialogs

import androidx.appcompat.app.AlertDialog
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.dialogs.ConfirmationDialog
import org.skywaves.mediavox.core.dialogs.FilePickerDialog
import org.skywaves.mediavox.core.extensions.*
import org.skywaves.mediavox.R
import org.skywaves.mediavox.databinding.DialogExportFavoritesBinding
import org.skywaves.mediavox.extensions.config

class ExportFavoritesDialog(
    val activity: BaseSimpleActivity, val defaultFilename: String, val hidePath: Boolean,
    callback: (path: String, filename: String) -> Unit
) {
    init {
        val lastUsedFolder = activity.config.lastExportedFavoritesFolder
        var folder = if (lastUsedFolder.isNotEmpty() && activity.getDoesFilePathExist(lastUsedFolder)) {
            lastUsedFolder
        } else {
            activity.internalStoragePath
        }

        val binding = DialogExportFavoritesBinding.inflate(activity.layoutInflater).apply {
            exportFavoritesFilename.setText(defaultFilename.removeSuffix(".txt"))

            if (hidePath) {
                exportFavoritesPathLabel.beGone()
                exportFavoritesPath.beGone()
            } else {
                exportFavoritesPath.text = activity.humanizePath(folder)
                exportFavoritesPath.setOnClickListener {
                    FilePickerDialog(activity, folder, false, showFAB = true) {
                        exportFavoritesPath.text = activity.humanizePath(it)
                        folder = it
                    }
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok, null)
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.export_favorite_paths) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        var filename = binding.exportFavoritesFilename.value
                        if (filename.isEmpty()) {
                            activity.toast(org.skywaves.mediavox.core.R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        filename += ".txt"
                        val newPath = "${folder.trimEnd('/')}/$filename"
                        if (!newPath.getFilenameFromPath().isAValidFilename()) {
                            activity.toast(org.skywaves.mediavox.core.R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        activity.config.lastExportedFavoritesFolder = folder
                        if (!hidePath && activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(org.skywaves.mediavox.core.R.string.file_already_exists_overwrite),
                                newPath.getFilenameFromPath()
                            )
                            ConfirmationDialog(activity, title) {
                                callback(newPath, filename)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newPath, filename)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
