package cz.ikem.dci.zscanner.persistence

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class TypeRepository(private val typeDao: TypeDao) {

    private val TAG = TypeRepository::class.java.simpleName

    val allTypes: LiveData<List<Type>> = typeDao.getAllDTs()

    @WorkerThread
    fun updateTypesTransaction(dts: List<Type>) {
        typeDao.updateTypesTransaction(dts)
    }

}