package cz.ikem.dci.zscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TypeDao {

    @Query("select * from document_type")
    fun getAllDTs(): LiveData<List<Type>>

    @Insert
    fun insert(dt: Type)

    @Query("delete from document_type")
    fun deleteAll()

    @Transaction
    fun updateTypesTransaction(types: List<Type>) {
        deleteAll()
        types.forEach {
            insert(it)
        }
    }

    @Query("select count(*) from document_type")
    fun count(): Int

    @Transaction
    fun updateTypesTransactionIfEmpty(types: List<Type>) {
        val cnt = count()
        if (cnt == 0) {
            deleteAll()
            types.forEach {
                insert(it)
            }
        }
    }

}