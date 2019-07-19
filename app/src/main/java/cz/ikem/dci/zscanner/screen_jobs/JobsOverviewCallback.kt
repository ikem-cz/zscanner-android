package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*


class JobsOverviewCallback(val adapter: JobsOverviewAdapter, private val ctx: Context) : ItemTouchHelper.Callback() {

    private val TAG = JobsOverviewCallback::class.java.simpleName

    var currentViewSwipable = false // If last view can be swiped away (job is finished) or not

    override fun onMove(p0: androidx.recyclerview.widget.RecyclerView, p1: androidx.recyclerview.widget.RecyclerView.ViewHolder, p2: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
        val id = adapter.items[viewHolder.adapterPosition].instanceId
        JobUtils(ctx).purgeJob(id)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
        return Callback.makeMovementFlags(0, LEFT or RIGHT);
    }

    override fun onChildDraw(c: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (currentViewSwipable) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX / 6, dY, actionState, isCurrentlyActive)
        }
    }

    override fun getSwipeThreshold(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Float {
        return if (currentViewSwipable) {
            0.5f
        } else {
            Float.MAX_VALUE
        }
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return if (currentViewSwipable) {
            defaultValue
        } else {
            Float.MAX_VALUE
        }
    }

    override fun onSelectedChanged(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, actionState: Int) {
        // this is called as soon as user touches the view. Determine here, if current card can be swiped away.
        if (viewHolder != null) {
            currentViewSwipable = adapter.items[viewHolder.adapterPosition].status != JobsOverviewAdapter.SendJobInfoStatus.RUNNING
        }
        super.onSelectedChanged(viewHolder, actionState)
    }
}
