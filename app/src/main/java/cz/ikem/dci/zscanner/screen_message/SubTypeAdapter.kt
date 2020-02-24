package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.DocumentSubType
import kotlinx.android.synthetic.main.item_row.view.*


class SubTypeAdapter(
        val context: Context
) : ListAdapter<DocumentSubType, SubTypeAdapter.ViewHolder>(diffCallback) {

    /** Callback when user click on holder */
    var onItemSelected: (item: DocumentSubType) -> Unit = {}

    /**
     * Holds selected item index
     *
     * Null means that none is selected.
     **/
    var selectedRow: Int? = null
        private set


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)

        return ViewHolder(itemView)
    }


    override fun onBindViewHolder(holderReceipt: ViewHolder, position: Int) {
        val item = getItem(position)

        holderReceipt.bind(item) {
            selectedRow = if (selectedRow == position) null else position
            notifyDataSetChanged()
            onItemSelected(it)
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)

        holder.itemView.clearAnimation()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: DocumentSubType, listener: (DocumentSubType) -> Unit) = with(itemView) {
            // Views
            item_text_view.text = item.display

            setOnClickListener { listener(item) }
        }
    }


    companion object {
        var diffCallback: DiffUtil.ItemCallback<DocumentSubType> =
                object : DiffUtil.ItemCallback<DocumentSubType>() {

                    override fun areItemsTheSame(oldItem: DocumentSubType, newItem: DocumentSubType): Boolean {
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(oldItem: DocumentSubType, newItem: DocumentSubType): Boolean {
                        return oldItem.equals(newItem)
                    }
                }
    }
}
