package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cz.ikem.dci.zscanner.KEY_DEPARTMENT
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Department
import cz.ikem.dci.zscanner.persistence.SendJob
import cz.ikem.dci.zscanner.screen_message.CreateMessageActivity
import kotlinx.android.synthetic.main.fragment_jobs_overview.*
import kotlinx.android.synthetic.main.fragment_jobs_overview.view.*

class JobsOverviewFragment : androidx.fragment.app.Fragment() {

    private val TAG = JobsOverviewFragment::class.java.simpleName

    private lateinit var mViewModel: JobsOverviewViewModel
    private lateinit var departmentViewModel: DepartmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(JobsOverviewViewModel::class.java)
        departmentViewModel = ViewModelProviders.of(this).get(DepartmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_jobs_overview, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createJobsAdapter()
        createDepartmentAdapter(this.requireContext())
    }


    private fun createJobsAdapter(){
        val jobsAdapter = JobsOverviewAdapter()
        jobs_recycler_view?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = jobsAdapter
            view?.let{
                setEmptyView(it.jobs_empty_view)
            }
        }
        mViewModel.storedSendJobs.observe(viewLifecycleOwner, Observer<List<SendJob>> { list ->
            if (list != null) {
                Log.d(TAG, "Observed ${list.size} jobs.")
            }
            if (list != null) {
                jobsAdapter.updateSendJobInfos(list)
            }
        })
        activity?.let { _activity ->
            val jobsTouchCallback = JobsOverviewCallback(jobsAdapter, _activity)
            val itemTouchHelper = JobsOverviewItemTouchHelper(jobsTouchCallback)
            itemTouchHelper.attachToRecyclerView(jobs_recycler_view)
        }
    }

    private fun createDepartmentAdapter(context: Context) {
        val departmentAdapter = DepartmentsAdapter(context)

        departmentAdapter.onItemSelected = {department ->
            val intent = Intent(context, CreateMessageActivity::class.java).apply {
                putExtras(
                        Bundle().apply {
                            putSerializable(KEY_DEPARTMENT, department)
                        }
                )
            }
            departmentViewModel.chosenDepartment.value = department
             startActivity(intent)
        }

        departments_recycler_view.adapter = departmentAdapter
        departments_recycler_view.layoutManager = LinearLayoutManager(context)

        departmentViewModel.storedDepartments.observe(viewLifecycleOwner, androidx.lifecycle.Observer { list: List<Department>? ->
            departmentAdapter.submitList(list)
        })
    }
}
