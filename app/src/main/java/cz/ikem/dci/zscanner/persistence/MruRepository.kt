package cz.ikem.dci.zscanner.persistence

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class MruRepository(private val mruDao: MruDao) {
    private val TAG = MruRepository::class.java.simpleName

    @WorkerThread
    fun mru(): LiveData<List<Mru>> {
        return mruDao.mru()
    }

    @WorkerThread
    fun insert(mru: Mru) {
        mruDao.insert(mru)
    }

    // insert and trim renudnant Mrus
    @WorkerThread
    fun smartInsert(mru: Mru) {
        mruDao.smartInsert(mru)
    }

}