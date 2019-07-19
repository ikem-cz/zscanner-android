package cz.ikem.dci.zscanner.screen_jobs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.persistence.SendJob

class JobsOverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = JobsOverviewViewModel::class.java.simpleName

    val storedSendJobs: LiveData<List<SendJob>> = Repositories(application).jobsRepository.allJobs

}