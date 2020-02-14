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

        Log.d(TAG, "SendSummaryWorker ${taskid} starts")

        val correlation = RequestBody.create(MediaType.parse("text/plain"), instance)

        try {

            val patid = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_PAT_ID))
            val type = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DOC_TYPE))
            val subType = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DOC_SUB_TYPE))
            val date = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DATE_STRING))
            val name = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_NAME))
            val note = RequestBody.create(MediaType.parse("text/plain"), inputData.getString(KEY_DOCUMENT_NOTE))


            Log.d(TAG, "SendSummaryWorker ${taskid} data: ${inputData.getString(KEY_PAT_ID)} ${inputData.getString(KEY_DOC_TYPE)} " +
                    "${inputData.getString(KEY_DOC_SUB_TYPE)} ${inputData.getString(KEY_DATE_STRING)} ${inputData.getString(KEY_NAME)}")

            val numpagesInt = inputData.getInt(KEY_NUM_PAGES, -1)
            if (numpagesInt == -1) {
                throw Exception("Assertion error")
            }

            val numpages = RequestBody.create(MediaType.parse("text/plain"), numpagesInt.toString())

            val res = HttpClient().getApiServiceBackend().postDocumentSummary(
                    correlation,
                    patid,
                    type,
                    subType,
                    numpages,
                    date,
                    RequestBody.create(MediaType.parse("text/plain"), "")
            ).execute()

            if (res.code() != 200) {
                throw Exception("Non OK response")
            }

            if (mCancelling) {
                throw Exception("Cancelling")
            }

            val repository = Repositories(applicationContext).jobsRepository

            repository.setPartialJobDoneTag(instance, taskid)

            Log.d(TAG, "SendSummaryWorker ${taskid} ends")

            return Result.success()

        } catch (e: Exception) {
            Log.d(TAG, "SendSummaryWorker ${taskid} caught exception !")

            Log.e(TAG, e.toString())
            return Result.retry()

        }


    }

    override fun onStopped() {
        mCancelling = true
        super.onStopped()
    }
}