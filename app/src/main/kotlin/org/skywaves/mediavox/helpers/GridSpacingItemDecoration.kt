package org.skywaves.mediavox.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.skywaves.mediavox.models.Medium
import org.skywaves.mediavox.models.ThumbnailItem

class GridSpacingItemDecoration(
    val spanCount: Int,
    var items: ArrayList<ThumbnailItem>, val useGridPosition: Boolean
) : RecyclerView.ItemDecoration() {

    override fun toString() = "spanCount: $spanCount, " +
        "items: ${items.hashCode()}, useGridPosition: $useGridPosition"

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val position = parent.getChildAdapterPosition(view)
        val medium = items.getOrNull(position) as? Medium ?: return
        val gridPositionToUse = if (useGridPosition) medium.gridPosition else position
        val column = gridPositionToUse % spanCount


}
}
