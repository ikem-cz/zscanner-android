package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.persistence.Type
import cz.ikem.dci.zscanner.webservices.HttpClient

class RefreshDocumentTypesWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    private val TAG = RefreshDocumentTypesWorker::class.java.simpleName


    override fun doWork(): Result {

        try {
            Log.d(TAG, "RefreshDocumentTypesWorker starting ..")

            val repository = Repositories(applicationContext).typeRepository

            val res = HttpClient().getApiServiceBackend().documentTypes.execute()

            val types = res.body()?.map { e ->
                Type(e.get("id").asString, e.get("display").asString)
            }

            types?.let{ repository.updateTypesTransaction(types) }

            Log.d(TAG, "RefreshDocumentTypesWorker terminating ..")

            return Result.success()

        } catch (ex: Exception) {

            return Result.retry()

        }
    }
}
