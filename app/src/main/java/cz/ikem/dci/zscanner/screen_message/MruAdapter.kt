package cz.ikem.dci.zscanner.screen_message

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ikem.dci.zscanner.MruSelectionCallback
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Mru
import kotlinx.android.synthetic.main.mru_row.view.*

class MruAdapter(val callback: MruSelectionCallback) : androidx.recyclerview.widget.RecyclerView.Adapter<MruAdapter.ViewHolder>() {

    private val TAG = MruAdapter::class.java.simpleName

    var items: List<Mru> = listOf()

    class ViewHolder(val mItemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mItemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.mru_row, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mru = items[position]
        holder.mItemView.mru_name_textview.text = mru.name
        holder.mItemView.mru_bid_textview.text = mru.bid
        holder.mItemView.setOnClickListener {
            callback.onMruSelected(mru)
        }
    }
}