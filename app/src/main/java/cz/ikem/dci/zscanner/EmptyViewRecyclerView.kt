package cz.ikem.dci.zscanner

import android.content.Context
import android.util.AttributeSet
import android.view.View


class EmptyViewRecyclerView : androidx.recyclerview.widget.RecyclerView {
    private var mEmptyView: View? = null

    private val mDataObserver = object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            updateEmptyView()
        }

        override fun onChanged() {
            super.onChanged()
            updateEmptyView()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            updateEmptyView()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            updateEmptyView()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            updateEmptyView()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            updateEmptyView()
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * Designate a view as the empty view. When the backing adapter has no
     * data this view will be made visible and the recycler view hidden.
     *
     */
    fun setEmptyView(emptyView: View) {
        mEmptyView = emptyView
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (getAdapter() != null) {
            getAdapter()!!.unregisterAdapterDataObserver(mDataObserver)
        }
        adapter?.registerAdapterDataObserver(mDataObserver)
        super.setAdapter(adapter)
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (mEmptyView != null && adapter != null) {
            val showEmptyView = adapter!!.itemCount == 0
            mEmptyView?.visibility = if (showEmptyView) View.VISIBLE else View.GONE
            visibility = if (showEmptyView) View.GONE else View.VISIBLE
        }
    }
}