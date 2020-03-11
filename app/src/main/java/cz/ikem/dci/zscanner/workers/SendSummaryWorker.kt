package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.webservices.HttpClient
import org.json.JSONObject




class SendSummaryWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    @Volatile
    private var mCancelling = false

    private val TAG = SendSummaryWorker::class.java.simpleName

    override fun doWork(): Result {

        val correlation = inputData.getString(KEY_CORRELATION_ID)

        // summary sending task externalId must contain substring "-S" -- is used for progress indicator calculations in JobsOverviewAdapter
        val taskid = (correlation?.substring(0, 6)) + "-S"

        Log.d(TAG, "SendSummaryWorker $taskid starts")

//        try {
            val numPagesInt = inputData.getInt(KEY_NUM_PAGES, -1)
            if (numPagesInt == -1) {
                throw Exception("Assertion error")
            }
//
//            val correlation = RequestBody.create(MediaType.parse("application/json"), instance)
//            val internalId = RequestBody.create(MediaType.parse("application/json"), inputData.getString(KEY_FOLDER_INTERNAL_ID))
//            val documentType = RequestBody.create(MediaType.parse("application/json"), inputData.getString(KEY_DOC_TYPE))
//            val documentSubType = RequestBody.create(MediaType.parse("application/json"), inputData.getString(KEY_DOC_SUB_TYPE))
//            val department = RequestBody.create(MediaType.parse("application/json"), inputData.getString(KEY_DEPARTMENT))
//            val numPages = RequestBody.create(MediaType.parse("application/json"), numPagesInt.toString())
//            val date = RequestBody.create(MediaType.parse("application/json"), inputData.getString(KEY_DATE_STRING))
//
//
//
//
//
//            Log.d(TAG, "SendSummaryWorker $taskid: correlation = $instance, internalId = ${inputData.getString(KEY_FOLDER_INTERNAL_ID)}, type = ${inputData.getString(KEY_DOC_TYPE)}, " +
//                    "subType = ${inputData.getString(KEY_DOC_SUB_TYPE)}, department = ${inputData.getString(KEY_DEPARTMENT)}, pages = ${inputData.getString(KEY_NUM_PAGES)}, date = ${inputData.getString(KEY_DATE_STRING)}")



        val internalId = inputData.getString(KEY_FOLDER_INTERNAL_ID)
        val documentType = inputData.getString(KEY_DOC_TYPE)
        val documentSubType = inputData.getString(KEY_DOC_SUB_TYPE)
        val department =  inputData.getString(KEY_DEPARTMENT)
        var numPages =  inputData.getString(numPagesInt.toString())
        val date = inputData.getString(KEY_DATE_STRING)


        val paramObject = JSONObject()
        paramObject.put("correlation", correlation)
        paramObject.put("folderInternalId", inputData.getString(KEY_FOLDER_INTERNAL_ID))
        paramObject.put("documentType", inputData.getString(KEY_DOC_TYPE))
        paramObject.put("department", inputData.getString(KEY_DEPARTMENT))
        paramObject.put("documentSubType",inputData.getString(KEY_DOC_SUB_TYPE))

        paramObject.put("pages", numPagesInt)
        paramObject.put("datetime", inputData.getString(KEY_DATE_STRING))

        Log.e("DEBUGGING", "SendSummaryWorker, doWork: paramObject = $paramObject")

            val request = HttpClient.ApiServiceBackend.postDocumentSummary(
//                    RequestBody.create(MediaType.parse("application/json"), paramObject.toString())
//                    correlation,
//                    internalId,
//                    documentType,
//                    documentSubType,
//                    department,
//                    numPages,
//                    date
                    paramObject
            )

            val req = request.request()
            val body = req.body()
            val headers = req.headers()
            val method = req.method()

            val type = body?.contentType()


            Log.e("DEBUGGING", "SendSummaryWorker, doWork: req = $req")
            Log.e("DEBUGGING", "SendSummaryWorker, doWork: body = $body")
            Log.e("DEBUGGING", "SendSummaryWorker, doWork: headers = $headers")
        Log.e("DEBUGGING", "SendSummaryWorker, doWork: method = $method")
        Log.e("DEBUGGING", "SendSummaryWorker, doWork: contentType = $type")

               val response = request.execute()


            if (response.code() != 200) {
                Log.e(TAG, "Response on postDocumentSummary: response: $response")
                val code = response.code()
                throw Exception("Non OK response, response code: $code")
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

//        } catch (e: Exception) {
//            Log.e(TAG, "SendSummaryWorker $taskid caught exception: $e.toString()")
//            return Result.retry()
//        }
    }

    override fun onStopped() {
        mCancelling = true
        super.onStopped()
    }
}