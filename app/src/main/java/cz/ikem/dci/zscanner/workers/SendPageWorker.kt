package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.webservices.HttpClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SendPageWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    @Volatile
    private var mCancelling = false

    private val TAG = SendPageWorker::class.java.simpleName

    override fun doWork(): Result {

        val instance = inputData.getString(KEY_CORRELATION_ID)!!

        // page sending task externalId must contain substring "-P-" -- is used for progress indicator calculations in JobsOverviewAdapter
        val taskid = (instance.substring(0, 6)) + "-P-" + inputData.getInt(KEY_PAGE_INDEX, -1)

        Log.d(TAG, "SendPageWorker ${taskid} starts")

        val correlation = RequestBody.create(MediaType.parse("text/plain"), instance)
        val pageInt = inputData.getInt(KEY_PAGE_INDEX, -1)

        if (pageInt == -1) {
            throw Exception("Assertion error")
        }
        val pagenum = RequestBody.create(MediaType.parse("text/plain"), pageInt.toString())


        val note = inputData.getString(KEY_DOCUMENT_NOTE) ?: ""
        val description = RequestBody.create(MediaType.parse("text/plain"), note)


        try {

            val pageFilename = inputData.getString(KEY_PAGE_FILE)
            val filePart =
                    MultipartBody.Part.createFormData(
                            "page",
                            pageFilename?.substringAfterLast("/"),
                            RequestBody.create(
                                    MediaType.parse("image/jpeg"),
                                    File(pageFilename?: "image")
                            )
                    )
            val filePartList = listOf(filePart)

            val request = HttpClient.ApiServiceBackend.postDocumentPage(
                filePartList,
                correlation,
                pagenum,
                description
            )

            Log.e("DEBUGGING", "SendPageWorker, doWork: description = $note.")
            Log.e("DEBUGGING", "SendPageWorker, doWork: AAAAAAAAAAAA = ${request.request()}")

//            val response = request.execute()

//            if (response.code() != 200) {
//                throw Exception("Non OK response: $response")
//            }

            if (mCancelling) {
                throw Exception("Cancelling")
            }

            val repository = Repositories(applicationContext).jobsRepository

            repository.setPartialJobDoneTag(instance, taskid)

            Log.d(TAG, "SendPageWorker $taskid ends")

            return Result.success()

        } catch (e: Exception) {
            Log.d(TAG, "SendPageWorker $taskid caught exception !")
            Log.e(TAG, e.toString())
            return Result.retry()

        }


    }

    override fun onStopped() {
        mCancelling = true
        super.onStopped()
    }

}