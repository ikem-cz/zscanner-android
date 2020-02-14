package cz.ikem.dci.zscanner.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DepartmentDao {


    @Query("select * from department")
    fun getAll(): LiveData<List<Department>>

    @Query("select * from department")
    fun getAllSync(): List<Department>

    @Insert
    fun insert(department: Department)

    @Update
    fun update(department: Department)

    @Delete
    fun delete(department: Department)

    @Query("delete from department")
    fun deleteAll()

    @Query("select count(*) from department")
    fun count(): Int

    @Transaction
    fun updateDepartmentsTransaction(types: List<Department>) {
        deleteAll()
        types.forEach {
            insert(it)
        }
    }


    @Transaction
    fun initializeIfEmpty(departments: List<Department>) {
        val cnt = count()
        if (cnt == 0) {
            deleteAll()
            departments.forEach {
                insert(it)
            }
        }
    }
}