package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.screen_message.CreateMessageViewModel
import cz.ikem.dci.zscanner.webservices.HttpClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SendPageWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    @Volatile
    private var mCancelling = false

    val app = applicationContext as ZScannerApplication

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

        val fireBaseLogger = FireBaseLogger()
        // log firebase event
        fireBaseLogger.logEvent(PHOTO, SENDING_STARTED)


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

            val response = request.execute()

            if (response.code() == 403 || response.code() == 401) {
                CreateMessageViewModel(app).logoutOnHttpResponse.postValue(true)
                fireBaseLogger.logEvent(SENDING_PHOTO_FAILED, "response code: ${response.code()}")
                return Result.failure()
            }

            if (response.code() != 200) {
                fireBaseLogger.logEvent(SENDING_PHOTO_FAILED, "response code: ${response.code()}")
                throw Exception("Non OK response: $response")
            }

            if (mCancelling) {
                fireBaseLogger.logEvent(SENDING_PHOTO_FAILED, "cancelled")
                throw Exception("Cancelling")
            }

            val repository = Repositories(applicationContext).jobsRepository

            repository.setPartialJobDoneTag(instance, taskid)

            Log.d(TAG, "SendPageWorker $taskid ends")

            // log firebase event
            fireBaseLogger.logEvent(PHOTO, "Successfully sent $pageInt photos")

            return Result.success()

        } catch (e: Exception) {
            Log.d(TAG, "SendPageWorker $taskid caught exception !")
            Log.e(TAG, e.toString())
            fireBaseLogger.logEvent(SENDING_PHOTO_FAILED, "Exception: $e")
            return Result.retry()
        }
    }

    override fun onStopped() {
        mCancelling = true
        super.onStopped()
    }

    companion object {
        const val PHOTO = "photo"
        const val SENDING_PHOTO_FAILED = "sending_photo_failed"
        const val SENDING_STARTED = "sending_started"
    }
}
