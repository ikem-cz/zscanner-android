package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.screen_message.CreateMessageViewModel
import cz.ikem.dci.zscanner.webservices.HttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class SendSummaryWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    @Volatile
    private var mCancelling = false

    val app = applicationContext as ZScannerApplication

    private val TAG = SendSummaryWorker::class.java.simpleName

    override fun doWork(): Result {

        val correlation = inputData.getString(KEY_CORRELATION_ID)

        // summary sending task externalId must contain substring "-S" -- is used for progress indicator calculations in JobsOverviewAdapter
        val taskid = (correlation?.substring(0, 6)) + "-S"

        Log.d(TAG, "SendSummaryWorker $taskid starts")

        try {
            val numPagesInt = inputData.getInt(KEY_NUM_PAGES, -1)
            if (numPagesInt == -1) {
                throw Exception("Assertion error")
            }


            val paramObject = JSONObject()
            paramObject.put("correlation", correlation)
            paramObject.put("folderInternalId", inputData.getString(KEY_FOLDER_INTERNAL_ID))
            paramObject.put("documentType", inputData.getString(KEY_DOC_TYPE))
            paramObject.put("department", inputData.getString(KEY_DEPARTMENT))
            paramObject.put("documentSubType", inputData.getString(KEY_DOC_SUB_TYPE))
            paramObject.put("pages", numPagesInt)
            paramObject.put("datetime", inputData.getString(KEY_DATE_STRING))
            paramObject.put("name", "") // TODO: maybe add some name or else remove this from iOS as well

            val reqBody = paramObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val request = HttpClient.ApiServiceBackend.postDocumentSummary(
                    reqBody
            )

            val response = request.execute()

            if (response.code() == 403 || response.code() == 401) {
                CreateMessageViewModel(app).logoutOnHttpResponse.postValue(true)
                return Result.failure()
            }

            if (response.code() != 200) {
                throw Exception("Non OK response, response code: ${response.code()}")
            }

            if (mCancelling) {
                throw Exception("Cancelling")
            }

            val repository = Repositories(applicationContext).jobsRepository
            correlation?.let {
                repository.setPartialJobDoneTag(it, taskid)
            }


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