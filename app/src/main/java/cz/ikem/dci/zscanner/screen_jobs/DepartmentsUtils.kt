package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import androidx.work.WorkManager
import cz.ikem.dci.zscanner.Utils
import cz.ikem.dci.zscanner.WORKTAG_REFRESH_DEPARTMENTS
import cz.ikem.dci.zscanner.persistence.Repositories

class DepartmentsUtils(private val context: Context) {

    private val TAG = JobUtils::class.java.simpleName


    /**
     * Cancel and clear all departments (used when user logs out)
     */
    fun nukeAllDepartments() {

        val workManager = WorkManager.getInstance()
        workManager.cancelAllWorkByTag(WORKTAG_REFRESH_DEPARTMENTS)

        Utils.dispatch {
            val repository = Repositories(context).departmentRepository
            repository.deleteAllDepartments()
        }
        workManager.pruneWork()
    }

}