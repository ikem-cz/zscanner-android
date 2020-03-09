package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.webservices.HttpClient
import okhttp3.MediaType
import okhttp3.RequestBody

class SendSummaryWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    @Volatile
    private var mCancelling = false

    private val TAG = SendSummaryWorker::class.java.simpleName

    override fun doWork(): Result {

        val instance = inputData.getString(KEY_CORRELATION_ID)!!

        // summary sending task externalId must contain substring "-S" -- is used for progress indicator calculations in JobsOverviewAdapter
        val taskid = (instance.substring(0, 6)) + "-S"

        Log.d(TAG, "SendSummaryWorker $taskid starts")

        try {
            val correlation = RequestBody.create(MediaType.parse("text/plain"), instance)
            val internalId = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_FOLDER_INTERNAL_ID))
            val type = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DOC_TYPE))
            val subType = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DOC_SUB_TYPE))
            val department = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DEPARTMENT))
            val numPagesInt = inputData.getInt(KEY_NUM_PAGES, -1)
            val date = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DATE_STRING))
       

            if (numPagesInt == -1) {
                throw Exception("Assertion error")
            }

            val numPages = RequestBody.create(MediaType.parse("text/plain"), numPagesInt.toString())

            Log.d(TAG, "SendSummaryWorker $taskid: correlation = $instance, internalId = ${inputData.getString(KEY_FOLDER_INTERNAL_ID)}, type = ${inputData.getString(KEY_DOC_TYPE)}, " +
                    "subType = ${inputData.getString(KEY_DOC_SUB_TYPE)}, department = ${inputData.getString(KEY_DEPARTMENT)}, pages = ${inputData.getString(KEY_NUM_PAGES)}, date = ${inputData.getString(KEY_DATE_STRING)}")

            val response = HttpClient.ApiServiceBackend.postDocumentSummary(
                    correlation,
                    internalId,
                    type,
                    subType,
                    department,
                    numPages,
                    date
            ).execute()


            if (response.code() != 200) {
                Log.e(TAG, "Response on postDocumentSummary: response: $response")
                val code = response.code()
                throw Exception("Non OK response, response code: $code")
            }

            if (mCancelling) {
                throw Exception("Cancelling")
            }

            val repository = Repositories(applicationContext).jobsRepository

            repository.setPartialJobDoneTag(instance, taskid)

            Log.d(TAG, "SendSummaryWorker $taskid ends")

            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "SendSummaryWorker $taskid caught exception: $e.toString()")
            return Result.retry()
        }
    }

    override fun onStopped() {
        mCancelling = true
        super.onStopped()
    }
}