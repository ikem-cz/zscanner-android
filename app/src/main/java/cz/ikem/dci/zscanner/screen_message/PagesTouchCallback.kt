package cz.ikem.dci.zscanner.screen_message

import androidx.recyclerview.widget.ItemTouchHelper

class PagesTouchCallback(private val vm: CreateMessageViewModel) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onMoved(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, fromPos: Int, target: androidx.recyclerview.widget.RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
        vm.move(viewHolder.layoutPosition, target.layoutPosition)
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.layoutPosition
        vm.removeAt(pos)
    }

}