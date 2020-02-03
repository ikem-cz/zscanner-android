package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.SendJob
import kotlinx.android.synthetic.main.fragment_jobs_overview.*
import kotlinx.android.synthetic.main.fragment_jobs_overview.view.*

class JobsOverviewFragment : androidx.fragment.app.Fragment() {

    private val TAG = JobsOverviewFragment::class.java.simpleName

    private lateinit var mViewModel: JobsOverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(JobsOverviewViewModel::class.java)
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
        mViewModel.storedSendJobs.observe(this, Observer<List<SendJob>> { list ->
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

        departmentAdapter.onItemSelected = {
            //TODO
        }

        departments_recycler_view.adapter = departmentAdapter
        departments_recycler_view.layoutManager = LinearLayoutManager(context)

        val mockListOfDepartments = mutableListOf<Department>()
        mockListOfDepartments.add(Department(null, "CHK-AMBDB - Ambulance dětské chirurgie B"))
        mockListOfDepartments.add(Department(null, "CHK-AMBS -  Chirurgická ambulance Smet.sady"))
        mockListOfDepartments.add(Department(null, "CHK-JIP1 - JIP I"))
        mockListOfDepartments.add(Department(null, "CHK-AMBB - Chirurgická ambulance Bory"))
        mockListOfDepartments.add(Department(null, "CHK-JIP2 - JIP II"))
        mockListOfDepartments.add(Department(null, "CHK-AMBG - Gastroenterologická ambulance"))
        mockListOfDepartments.add(Department(null, "CHK-PRIJK - Přijímací kancelář"))
        mockListOfDepartments.add(Department(null, "CHK-KANC - Kancelář"))

        departmentAdapter.submitList(mockListOfDepartments)

        //TODO add view model
//        val viewModel = DepartmentViewModel()
//        viewModel.activeLiveData.observe(this, androidx.lifecycle.Observer { list: List<Department>? ->
//            departmentAdapter.submitList(list)
//        })
    }

}
