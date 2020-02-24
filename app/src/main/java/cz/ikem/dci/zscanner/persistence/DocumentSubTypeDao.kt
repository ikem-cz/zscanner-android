package cz.ikem.dci.zscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface DocumentSubTypeDao {

    @Query("select * from document_sub_type")
    fun getAllSubTypes(): LiveData<List<DocumentSubType>>

    @Insert
    fun insert(st: DocumentSubType)

    @Query("delete from document_sub_type")
    fun deleteAll()

    @Transaction
    fun updateSubTypesTransaction(documentSubType: List<DocumentSubType>) {
        deleteAll()
        documentSubType.forEach {
            insert(it)
        }
    }

    @Query("select count(*) from document_sub_type")
    fun count(): Int

    @Transaction
    fun updateSubTypesTransactionIfEmpty(documentSubTypes: List<DocumentSubType>) {
        val cnt = count()
        if (cnt == 0) {
            deleteAll()
            documentSubTypes.forEach {
                insert(it)
            }
        }
    }

}