package cz.ikem.dci.zscanner.screen_jobs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cz.ikem.dci.zscanner.EmptyViewRecyclerView
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.SendJob
import kotlinx.android.synthetic.main.fragment_jobs_overview.view.*

class JobsOverviewFragment : androidx.fragment.app.Fragment() {

    private val TAG = JobsOverviewFragment::class.java.simpleName

    private var mRecyclerView: EmptyViewRecyclerView? = null
    private lateinit var mViewModel: JobsOverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(this).get(JobsOverviewViewModel::class.java)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_jobs_overview, container, false)

        val jobsAdapter = JobsOverviewAdapter()
        mRecyclerView = (view.jobs_recycler_view).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = jobsAdapter
            setEmptyView(view.jobs_empty_view)
        }

        mViewModel.storedSendJobs.observe(this, Observer<List<SendJob>> { t ->
            if (t != null) {
                Log.d(TAG, "Observed ${t.size} jobs.")
            }
            if (t != null) {
                jobsAdapter.updateSendJobInfos(t)
            }
        })

        val jobsTouchCallback = JobsOverviewCallback(jobsAdapter, activity!!)
        val itemTouchHelper = JobsOverviewItemTouchHelper(jobsTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        return view

    }

}
