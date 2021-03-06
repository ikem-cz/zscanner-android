package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.webservices.HttpClient
import cz.ikem.dci.zscanner.webservices.Patient
import kotlinx.android.synthetic.main.patient_suggestion_row.view.*

class PatientAdapter(private val mContext: Context, val mViewModel: CreateMessageViewModel) : BaseAdapter(), Filterable {

    private val TAG = PatientAdapter::class.java.simpleName

    var mSuggestions: List<Patient> = listOf()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View?
        view = if (convertView == null) {
            val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.patient_suggestion_row, parent, false)!!
        } else {
            convertView
        }

        view.suggestion_name_textview.text = mSuggestions[position].name
        view.suggestion_bid_textview.text = mSuggestions[position].bid
        return view

    }

    override fun getItem(position: Int): Patient {
        return mSuggestions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mSuggestions.size
    }

    private val mFilter: Filter = object : Filter() {

        private var lastFilteredConstraint : String = ""

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {

            lastFilteredConstraint = constraint.toString()

            mViewModel.loadingSuggestions.postValue(true)
            mViewModel.tooManySuggestions.postValue(false)

                try {

                    val filterResults = Filter.FilterResults()
                    if (constraint != null) {

                        val response = HttpClient().getApiServiceBackend().searchPatients(constraint.toString()).execute()

                        // Assign the data to the FilterResults
                        filterResults.values = response.body()!!
                        filterResults.count = response.body()!!.count()
                    }

                    return filterResults
                } catch (e: Exception) {

                    return Filter.FilterResults()
                }
        }

        override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {

            if (constraint.toString() == lastFilteredConstraint) {
                mViewModel.loadingSuggestions.postValue(false)
                if (((constraint != null) && (constraint.toString().length >= 3)) && (results == null || results.count == 0)) { // if above completion threshold and cannot suggest anything
                    mViewModel.noSuggestions.postValue(true)
                } else {
                    mViewModel.noSuggestions.postValue(false)
                }
                if ((results != null) && (results.count > 15)) {
                    mViewModel.tooManySuggestions.postValue(true)
                } else {
                    mViewModel.tooManySuggestions.postValue(false)
                }
            }

            if (results != null && results.count > 0) {
                mSuggestions = results.values as List<Patient>
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }

        }

    }

    override fun getFilter(): Filter {

        return mFilter

    }

}