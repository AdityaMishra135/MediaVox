package org.skywaves.mediavox.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.skywaves.mediavox.models.Medium
import org.skywaves.mediavox.models.ThumbnailItem

class GridSpacingItemDecoration(
    val spanCount: Int, val spacing: Int, val addSideSpacing: Boolean,
    var items: ArrayList<ThumbnailItem>, val useGridPosition: Boolean
) : RecyclerView.ItemDecoration() {

    override fun toString() = "spanCount: $spanCount, spacing: $spacing, addSideSpacing: $addSideSpacing, " +
        "items: ${items.hashCode()}, useGridPosition: $useGridPosition"

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (spacing <= 1) {
            return
        }

        val position = parent.getChildAdapterPosition(view)
        val medium = items.getOrNull(position) as? Medium ?: return
        val gridPositionToUse = if (useGridPosition) medium.gridPosition else position
        val column = gridPositionToUse % spanCount


            if (addSideSpacing) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                outRect.bottom = spacing

                if (position < spanCount && !useGridPosition) {
                    outRect.top = spacing
                }
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount

                if (gridPositionToUse >= spanCount) {
                    outRect.top = spacing
                }
            }
}
}
