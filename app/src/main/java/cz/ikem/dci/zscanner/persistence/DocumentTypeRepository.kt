package cz.ikem.dci.zscanner.persistence

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class DocumentTypeRepository(private val documentTypeDao: DocumentTypeDao) {

    private val TAG = DocumentTypeRepository::class.java.simpleName

    val allDocumentTypes: LiveData<List<DocumentType>> = documentTypeDao.getAllDTs()

    @WorkerThread
    fun updateDocumentTypesTransaction(dts: List<DocumentType>) {
        documentTypeDao.updateDocumentTypesTransaction(dts)
    }

}