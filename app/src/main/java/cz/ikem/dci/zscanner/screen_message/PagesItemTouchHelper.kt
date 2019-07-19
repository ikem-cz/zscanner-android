package cz.ikem.dci.zscanner.screen_message

import androidx.recyclerview.widget.ItemTouchHelper
import cz.ikem.dci.zscanner.OnStartDragListener

class PagesItemTouchHelper(callback: PagesTouchCallback) : ItemTouchHelper(callback), OnStartDragListener {
    override fun onStartDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        startDrag(viewHolder)
    }
}