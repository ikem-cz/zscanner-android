package cz.ikem.dci.zscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DocumentTypeDao {

    @Query("select * from document_type")
    fun getAllDTs(): LiveData<List<DocumentType>>

    @Insert
    fun insert(dt: DocumentType)

    @Query("delete from document_type")
    fun deleteAll()

    @Transaction
    fun updateDocumentTypesTransaction(documentTypes: List<DocumentType>) {
        deleteAll()
        documentTypes.forEach {
            insert(it)
        }
    }

    @Query("select count(*) from document_type")
    fun count(): Int

    @Transaction
    fun updateDocumentTypesTransactionIfEmpty(documentTypes: List<DocumentType>) {
        val cnt = count()
        if (cnt == 0) {
            deleteAll()
            documentTypes.forEach {
                insert(it)
            }
        }
    }

}