package com.ojomono.ionce.ui.talesList

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class TalesTouchCallback(private val adapter: TalesAdapter) : ItemTouchHelper.Callback() {

    // Keeps the starting position of the dragged item
    var oldPosition: Int? = null

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder)
            : Int {
        // Specify the directions of movement
        val dragFlags =
            if (viewHolder.itemViewType == TalesAdapter.ITEM_VIEW_TYPE_ITEM)
                ItemTouchHelper.UP or ItemTouchHelper.DOWN
            else ItemTouchHelper.ACTION_STATE_IDLE  // The header can't be moved
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Notify the adapter that an item is moved from x position to y position
        if (target.adapterPosition > 0)     // Allow dragging over the header without interruption
            adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        // true: if you want to start dragging on long press
        // false: if you want to handle it yourself
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        // Handle action state changes
        viewHolder?.let { oldPosition = it.adapterPosition }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // Called by the ItemTouchHelper when the user interaction with an element is over and it
        // also completed its animation.
        // This is a good place to send update to your backend about changes
        oldPosition?.let {
            // TODO return when drag n' drop feature is enabled
//            if (it != viewHolder.adapterPosition)
//                adapter.onRowMoved(it, viewHolder.adapterPosition)
            oldPosition = null
        }
    }
}