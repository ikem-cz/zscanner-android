package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.KEY_SUB_TYPE
import cz.ikem.dci.zscanner.KEY_TYPE
import cz.ikem.dci.zscanner.persistence.DocumentType
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.screen_message.CreateMessageTypeFragment
import cz.ikem.dci.zscanner.webservices.HttpClient
import org.json.JSONException
import org.json.JSONObject


class RefreshDocumentTypesWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val TAG = RefreshDocumentTypesWorker::class.java.simpleName

    val department = inputData.getString(CreateMessageTypeFragment.EXTRA_DEPARTMENT)


    override fun doWork(): Result {

        try {
            Log.d(TAG, "RefreshDocumentTypesWorker starting ..")

            val repository = Repositories(applicationContext).docTypeRepository

            department?.let {

                val response = HttpClient.ApiServiceBackend.getDocumentTypes(department).execute()
                if (response.code() != 200) {
                    return Result.retry()
                }

                val body = response.body()
                val bodyString = body?.toString()
                val bodyJson: JSONObject

                try {
                    bodyJson = JSONObject(bodyString)
                } catch (exception: JSONException) {
                    Log.e(TAG, "Can't parse the response (bodyJson): $exception")
                    return Result.retry()
                }
                // verify if has field types
                if (!bodyJson.has(KEY_TYPE)) {
                    Log.e(TAG, "Invalid response from the server: no document types")
                    return Result.failure()
                }

                val documentTypesJsonArray = bodyJson.getJSONArray(KEY_TYPE)

                val types = ArrayList<DocumentType>()
                for (type in 0 until documentTypesJsonArray.length()) {
                    val typeObject = documentTypesJsonArray.getJSONObject(type)

                    val id = typeObject.getString("id")
                    val display = typeObject.getString("display")
                    val typeIdPlusDocumentId = id + "_" + department


                    val docType = if (typeObject.has(KEY_SUB_TYPE)) {
                        DocumentType(docTypeIdPlusDeptId = typeIdPlusDocumentId, id = id, display = display, subtype = (typeObject.getString(KEY_SUB_TYPE)), departmentId = department)
                    } else {
                        DocumentType(docTypeIdPlusDeptId = typeIdPlusDocumentId, id = id, display = display, departmentId = department)
                    }
                    types.add(docType)

                }


                repository.updateDocumentTypesTransaction(types)

                Log.d(TAG, "RefreshDocumentTypesWorker terminating ..")
            }
            return Result.success()

        } catch (exception: Exception) {
            Log.e(TAG, "Can't parse the response: $exception")
            return Result.retry()

        }
    }
}
