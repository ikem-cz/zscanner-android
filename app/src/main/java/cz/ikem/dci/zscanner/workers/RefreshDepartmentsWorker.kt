package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.persistence.Department
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.webservices.HttpClient


class RefreshDepartmentsWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val TAG = RefreshDepartmentsWorker::class.java.simpleName


    override fun doWork(): Result {

        try {
            Log.d(TAG, "RefreshDepartmentsWorker starting ..")

            val repository = Repositories(applicationContext).departmentRepository

            val response = HttpClient().getApiServiceBackend().departments.execute()

            val listOfDepartments = mutableListOf<Department>()
            val departments = response.body()
            departments?.forEach { departmentJson ->
                listOfDepartments.add(Department(departmentJson["id"].asString, departmentJson["display"].asString))
            }

            repository.updateDepartmentsTransaction(listOfDepartments)

            Log.d(TAG, "RefreshDocumentTypesWorker terminating ..")

            return Result.success()

        } catch (ex: Exception) {

            return Result.retry()

        }
    }
}
