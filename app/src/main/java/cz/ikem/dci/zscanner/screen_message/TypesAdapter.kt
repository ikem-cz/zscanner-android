package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import cz.ikem.dci.zscanner.persistence.Type
import android.widget.TextView


class TypesAdapter(private val mContext: Context, typesDef: List<Type>) : BaseAdapter(), Filterable {

    val typesDefFiltered = typesDef

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        convertView?.let {
            (convertView as TextView).text = typesDefFiltered[position].display
            return convertView
        } ?: run {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(android.R.layout.simple_list_item_1, null) as TextView
            view.text = typesDefFiltered[position].display
            return view
        }
    }

    override fun getItem(position: Int): Type {
        return typesDefFiltered[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return typesDefFiltered.size
    }

    private val mFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return Filter.FilterResults().apply {
                values = typesDefFiltered
                count = typesDefFiltered.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }

    }

    override fun getFilter(): Filter {
        return mFilter
    }
}