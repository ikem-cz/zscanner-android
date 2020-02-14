package cz.ikem.dci.zscanner.persistence

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class DepartmentRepository(private val departmentDao: DepartmentDao) {
    private val TAG = DepartmentRepository::class.java.simpleName

    val allDepartments: LiveData<List<Department>> = departmentDao.getAll()

    @WorkerThread
    fun getAllSync(departments: Department) {
        departmentDao.getAllSync()
    }

    @WorkerThread
    fun updateDepartmentsTransaction(departments: List<Department>) {
        departmentDao.updateDepartmentsTransaction(departments)
    }

}