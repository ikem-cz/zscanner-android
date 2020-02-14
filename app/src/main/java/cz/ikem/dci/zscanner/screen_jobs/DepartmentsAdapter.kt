package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Department
import kotlinx.android.synthetic.main.department_row.view.*


class DepartmentsAdapter(
        val context: Context
) : ListAdapter<Department, DepartmentsAdapter.ViewHolder>(diffCallback) {

    /** Callback when user click on holder */
    var onItemSelected: (item: Department) -> Unit = {}


    /**
     * Holds selected item index
     *
     * Null means that none is selected.
     **/
    var selectedRow: Int? = null
        private set


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.department_row, parent, false)

        return ViewHolder(itemView)
    }


    override fun onBindViewHolder(holderReceipt: ViewHolder, position: Int) {
        val item = getItem(position)

        holderReceipt.bind(item) {
            selectedRow = if (selectedRow == position) null else position
            notifyDataSetChanged()
            onItemSelected(it)
        }
        holderReceipt.highlight(selectedRow == position)
    }

    /**
     * Deselect rows
     */
    fun deselectAll() {
        selectedRow = null
        notifyDataSetChanged()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)

        holder.itemView.clearAnimation()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun highlight(enabled: Boolean) = with(itemView) {
            department_row?.isSelected = enabled
            department_row?.background = if (enabled) {
                ColorDrawable(context.getColor(R.color.invalid))
            } else {
                ColorDrawable(context.getColor(R.color.colorTranslucent))
            }
        }

        fun bind(item: Department, listener: (Department) -> Unit) = with(itemView) {
            // Views
            department_text_view.text = "${item.id} - ${item.display}"

            setOnClickListener { listener(item) }
        }
    }


    companion object {
        var diffCallback: DiffUtil.ItemCallback<Department> =
                object : DiffUtil.ItemCallback<Department>() {

                    override fun areItemsTheSame(oldItem: Department, newItem: Department): Boolean {
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(oldItem: Department, newItem: Department): Boolean {
                        return oldItem.equals(newItem)
                    }
                }
    }
}
