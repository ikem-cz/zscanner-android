package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.persistence.Department
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.webservices.HttpClient


class RefreshDepartmentsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val TAG = RefreshDepartmentsWorker::class.java.simpleName


    override fun doWork(): Result {

        try {
            Log.d(TAG, "RefreshDepartmentsWorker starting ..")

            val repository = Repositories(applicationContext).departmentRepository

            val response = HttpClient().getApiServiceBackend(applicationContext as ZScannerApplication).departments.execute()

            val departments = response.body()?.map{ departmentJson ->
                Department(departmentJson.get("id").asString, departmentJson.get("display").asString)
            }

            departments?.let{ repository.updateDepartmentsTransaction(it) }

            Log.d(TAG, "RefreshDepartmentsWorker terminating ..")

            return Result.success()

        } catch (exception: Exception) {

            return Result.retry()

        }
    }
}
