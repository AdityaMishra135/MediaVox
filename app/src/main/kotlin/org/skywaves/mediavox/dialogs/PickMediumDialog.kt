package org.skywaves.mediavox.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import org.skywaves.mediavox.core.activities.BaseSimpleActivity
import org.skywaves.mediavox.core.extensions.getAlertDialogBuilder
import org.skywaves.mediavox.core.extensions.getProperPrimaryColor
import org.skywaves.mediavox.core.extensions.setupDialogStuff
import org.skywaves.mediavox.core.helpers.VIEW_TYPE_GRID
import org.skywaves.mediavox.core.views.MyGridLayoutManager
import org.skywaves.mediavox.R
import org.skywaves.mediavox.adapters.MediaAdapter
import org.skywaves.mediavox.asynctasks.GetMediaAsynctask
import org.skywaves.mediavox.databinding.DialogMediumPickerBinding
import org.skywaves.mediavox.extensions.config
import org.skywaves.mediavox.extensions.getCachedMedia
import org.skywaves.mediavox.helpers.GridSpacingItemDecoration
import org.skywaves.mediavox.helpers.SHOW_ALL
import org.skywaves.mediavox.models.Medium
import org.skywaves.mediavox.models.ThumbnailItem
import org.skywaves.mediavox.models.ThumbnailSection

class PickMediumDialog(val activity: BaseSimpleActivity, val path: String, val callback: (path: String) -> Unit) {
    private var dialog: AlertDialog? = null
    private var shownMedia = ArrayList<ThumbnailItem>()
    private val binding = DialogMediumPickerBinding.inflate(activity.layoutInflater)
    private val config = activity.config
    private val viewType = config.getFolderViewType(if (config.showAll) SHOW_ALL else path)
    private var isGridViewType = viewType == VIEW_TYPE_GRID

    init {
        (binding.mediaGrid.layoutManager as MyGridLayoutManager).apply {
            orientation =  RecyclerView.VERTICAL
            spanCount = if (isGridViewType) config.mediaColumnCnt else 1
        }

        binding.mediaFastscroller.updateColors(activity.getProperPrimaryColor())

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.skywaves.mediavox.core.R.string.ok, null)
            .setNegativeButton(org.skywaves.mediavox.core.R.string.cancel, null)
            .setNeutralButton(R.string.other_folder) { dialogInterface, i -> showOtherFolder() }
            .apply {
                activity.setupDialogStuff(binding.root, this, org.skywaves.mediavox.core.R.string.select_photo) { alertDialog ->
                    dialog = alertDialog
                }
            }

        activity.getCachedMedia(path) {
            val media = it.filter { it is Medium } as ArrayList
            if (media.isNotEmpty()) {
                activity.runOnUiThread {
                    gotMedia(media)
                }
            }
        }

        GetMediaAsynctask(activity, path, false, false, false) {
            gotMedia(it)
        }.execute()
    }

    private fun showOtherFolder() {
        PickDirectoryDialog(activity, path, true, true, false, false) {
            callback(it)
            dialog?.dismiss()
        }
    }

    private fun gotMedia(media: ArrayList<ThumbnailItem>) {
        if (media.hashCode() == shownMedia.hashCode())
            return

        shownMedia = media
        val adapter = MediaAdapter(activity, shownMedia.clone() as ArrayList<ThumbnailItem>, null, true, false, path, binding.mediaGrid) {
            if (it is Medium) {
                callback(it.path)
                dialog?.dismiss()
            }
        }

        binding.apply {
            mediaGrid.adapter = adapter
            mediaFastscroller.setScrollVertically(true)
        }
        handleGridSpacing(media)
    }

    private fun handleGridSpacing(media: ArrayList<ThumbnailItem>) {
        if (isGridViewType) {
            val spanCount = config.mediaColumnCnt
            val spacing = config.thumbnailSpacing
            val useGridPosition = media.firstOrNull() is ThumbnailSection

            var currentGridDecoration: GridSpacingItemDecoration? = null
            if (binding.mediaGrid.itemDecorationCount > 0) {
                currentGridDecoration = binding.mediaGrid.getItemDecorationAt(0) as GridSpacingItemDecoration
                currentGridDecoration.items = media
            }

            val newGridDecoration = GridSpacingItemDecoration(spanCount, spacing, config.fileRoundedCorners, media, useGridPosition)
            if (currentGridDecoration.toString() != newGridDecoration.toString()) {
                if (currentGridDecoration != null) {
                    binding.mediaGrid.removeItemDecoration(currentGridDecoration)
                }
                binding.mediaGrid.addItemDecoration(newGridDecoration)
            }
        }
    }
}
