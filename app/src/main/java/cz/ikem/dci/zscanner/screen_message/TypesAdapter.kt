package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ikem.dci.zscanner.persistence.DocumentType
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.ikem.dci.zscanner.R
import kotlinx.android.synthetic.main.item_row.view.*


class TypesAdapter(
        val context: Context
) : ListAdapter<DocumentType, TypesAdapter.ViewHolder>(diffCallback) {

    /** Callback when user click on holder */
    var onItemSelected: (item: DocumentType) -> Unit = {}

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

        fun bind(item: DocumentType, listener: (DocumentType) -> Unit) = with(itemView) {
            // Views
            item_text_view.text = item.display

            setOnClickListener { listener(item) }
        }
    }


    companion object {
        var diffCallback: DiffUtil.ItemCallback<DocumentType> =
                object : DiffUtil.ItemCallback<DocumentType>() {

                    override fun areItemsTheSame(oldItem: DocumentType, newItem: DocumentType): Boolean {
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(oldItem: DocumentType, newItem: DocumentType): Boolean {
                        return oldItem.equals(newItem)
                    }
                }
    }
}
