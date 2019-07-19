package cz.ikem.dci.zscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SendJobDao {

    @Query("select * from send_jobs order by timestamp desc")
    fun getAll(): LiveData<List<SendJob>>

    @Query("select * from send_jobs order by timestamp desc")
    fun getAllSync(): List<SendJob>

    @Insert
    fun insert(sendJob: SendJob)

    @Update
    fun update(sendJob: SendJob)

    @Delete
    fun delete(sendJob: SendJob)

    //@Query("update send_jobs set internal_done_tasks = internal_done_tasks + 1 where instance_id = :correlationId")
    //fun incrementDone(correlationId: String)

    @Query("delete from send_jobs where instance_id = :correlationId")
    fun deleteByInstanceId(correlationId: String)

    @Query("delete from send_jobs")
    fun deleteAll()

    @Query("select * from send_jobs where instance_id = :id")
    fun getByIdSync(id: String): SendJob

    @Transaction
    fun insertInTransaction(sendJob: SendJob) {
        insert(sendJob)
    }

    @Transaction
    fun deleteMultipleByInstanceIds(ids: List<String>) {
        ids.forEach {
            deleteByInstanceId(it)
        }
    }

    //@Transaction
    //fun incrementDoneInTransaction(correlationId: String) {
    //    incrementDone(correlationId)
    //}

    @Transaction
    fun setPartialJobDoneTag(jobId: String, donetag: String) {
        val job = getByIdSync(jobId)
        val done = job.intDoneTasks
        if (!done.contains(donetag)) {
            job.intDoneTasks = listOf(done, listOf(donetag)).flatten()
            update(job)
        }
    }
}