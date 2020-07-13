package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.*
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.Utils.Companion.dispatch
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.persistence.SendJob
import cz.ikem.dci.zscanner.screen_message.PageActionsQueue
import cz.ikem.dci.zscanner.webservices.Patient
import cz.ikem.dci.zscanner.workers.DirectoryCleanupWorker
import cz.ikem.dci.zscanner.workers.PageFilesCleanupWorker
import cz.ikem.dci.zscanner.workers.SendPageWorker
import cz.ikem.dci.zscanner.workers.SendSummaryWorker
import java.util.concurrent.TimeUnit

class JobUtils(private val context: Context) {

    private val TAG = JobUtils::class.java.simpleName

    /**
     *  Create and schedule sendjob of create message process result
     */
    fun addJob(
            instanceId: String,
            timestamp: Long,
            patient: Patient,
            documentType: String,
            documentSubType: String?,
            department: String,
            documentNote: String,
            dateString: String,
            toSend: List<PageActionsQueue.Page>,
            description: String
    ) {

        val sendJob = SendJob(
                instanceId,
                timestamp,
                patient.internalId,
                documentType,
                documentSubType,
                documentNote,
                dateString,
                toSend,
                toSend.count() + 1,
                listOf(),
                description,
                "${patient.externalId} ${patient.name}"
        )

        dispatch {
            val repository = Repositories(context).jobsRepository
            repository.insertSendJob(sendJob)
        }

        val sendSummaryWorkerData = Data.Builder()
                .putString(KEY_CORRELATION_ID, instanceId)
                .putString(KEY_FOLDER_INTERNAL_ID, patient.internalId)
                .putString(KEY_DOC_TYPE, documentType)
                .putString(KEY_DOC_SUB_TYPE, documentSubType)
                .putString(KEY_DEPARTMENT, department)
                .putInt(KEY_NUM_PAGES, toSend.size)
                .putString(KEY_DATE_STRING, dateString)
                .build()

        val sendSummaryWorker = OneTimeWorkRequest.Builder(SendSummaryWorker::class.java)
                .addTag(WORKTAG_SENDING_JOB)
                .setInputData(sendSummaryWorkerData)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
                .build()

        // make pages send workers
        val sendPageWorkerDatas = toSend.mapIndexed { index,  page ->
            Data.Builder()
                    .putString(KEY_CORRELATION_ID, instanceId)
                    .putInt(KEY_PAGE_INDEX, index)
                    .putString(KEY_PAGE_FILE, page.path) //path
                    .putString(KEY_DOCUMENT_NOTE, page.note) //note
                    .build()
        }


        val sendPageWorkers = sendPageWorkerDatas.map { e ->
            OneTimeWorkRequest.Builder(SendPageWorker::class.java).setInputData(e)
                    .addTag(WORKTAG_SENDING_JOB)
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
                    .build()
        }

        val allSendWorkers = listOf(listOf(sendSummaryWorker), sendPageWorkers).flatten()
        WorkManager.getInstance()
                .beginWith(allSendWorkers)
                .enqueue()
    }

    /**
     * Remove sendjob from database and clean up
     */
    fun purgeJob(id: String) {
        dispatch {
            val repository = Repositories(context).jobsRepository
            val sj = repository.getJobByIdSync(id)
            repository.deleteSendJobByInstanceId(id)
            JobUtils(context).scheduleFilesCleanup(sj.pageFiles, true, id)
        }
    }

    /**
     * Clear all finished sendjobs
     */
    fun clearFinishedJobs() {
        dispatch {
            val repository = Repositories(context).jobsRepository
            val done = repository.getDoneSync()
            val ids = done.map { it.correlationId }
            repository.deleteSendJobsByInstanceIds(ids)
            done.forEach {
                JobUtils(context).scheduleFilesCleanup(it.pageFiles, true, it.correlationId)
            }
        }

        val workManager = WorkManager.getInstance()
        workManager.pruneWork()
    }

    /**
     * Cancel and clear all sendjobs (used when user logs out)
     */
    fun nukeAllJobs() {

        val workManager = WorkManager.getInstance()
        workManager.cancelAllWorkByTag(WORKTAG_SENDING_JOB)

        dispatch {
            val repository = Repositories(context).jobsRepository
            val allJobs = repository.getAllSync()
            val ids = allJobs.map { it.correlationId }
            repository.deleteSendJobsByInstanceIds(ids)
            allJobs.forEach {
                JobUtils(context).scheduleFilesCleanup(it.pageFiles, true, it.correlationId)
            }
        }

        workManager.pruneWork()
    }

    fun scheduleFilesCleanup(files: List<PageActionsQueue.Page?>, deleteDir: Boolean = false, instanceId: String? = null) {
        val workManager = WorkManager.getInstance()
        for (file in files) {
            val data = Data.Builder()
                    .putString(KEY_PAGE_FILE, file?.path) //path
                    .putString(KEY_DOCUMENT_NOTE, file?.note) //note
                    .build()
            val request =
                    OneTimeWorkRequest.Builder(PageFilesCleanupWorker::class.java)
                            .setInputData(data)
                            .build()

            var workContinuation = workManager.beginWith(request)

            if (deleteDir) {
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + instanceId!!)!!.absolutePath
                val dirCleanupWorkerData = Data.Builder().putString(KEY_DIRECTORY, storageDir).build()
                val dirCleanupWorker = OneTimeWorkRequest.Builder(DirectoryCleanupWorker::class.java).setInputData(dirCleanupWorkerData).build()
                workContinuation = workContinuation.then(dirCleanupWorker)
            }

            workContinuation.enqueue()

        }
    }

}
