package cz.ikem.dci.zscanner.persistence

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class SendJobRepository(private val sendJobDao: SendJobDao) {

    private val TAG = SendJobRepository::class.java.simpleName

    val allJobs: LiveData<List<SendJob>> = sendJobDao.getAll()

    @WorkerThread
    fun insertSendJob(job: SendJob) {
        sendJobDao.insert(job)
    }

    @WorkerThread
    fun setPartialJobDoneTag(jobId: String, partialJobTag: String) {
        sendJobDao.setPartialJobDoneTag(jobId, partialJobTag)
    }

    @WorkerThread
    fun deleteSendJobByInstanceId(id: String) {
        Log.d(TAG, "Deleting by correlationId: ${id}")
        sendJobDao.deleteByInstanceId(id)
    }

    @WorkerThread
    fun deleteSendJobsByInstanceIds(ids: List<String>) {
        sendJobDao.deleteMultipleByInstanceIds(ids)
    }

    @WorkerThread
    fun getDoneSync(): List<SendJob> {
        val tasks = sendJobDao.getAllSync()
        return tasks.filter {
            it.intDoneTasks.size == it.intSumTasks
        }
    }

    @WorkerThread
    fun getAllSync(): List<SendJob> {
        return sendJobDao.getAllSync()
    }

    @WorkerThread
    fun getJobByIdSync(id: String): SendJob {
        return sendJobDao.getByIdSync(id)
    }


}

