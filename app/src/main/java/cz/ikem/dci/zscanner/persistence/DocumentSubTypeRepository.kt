package cz.ikem.dci.zscanner.persistence

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class DocumentSubTypeRepository(private val documentSubTypeDao: DocumentSubTypeDao) {

    private val TAG = DocumentSubTypeRepository::class.java.simpleName

    val allDocumentSubTypes: LiveData<List<DocumentSubType>> = documentSubTypeDao.getAllSubTypes()

    @WorkerThread
    fun updateSubTypesTransaction(subtypes: List<DocumentSubType>) {
        documentSubTypeDao.updateSubTypesTransaction(subtypes)
    }

}