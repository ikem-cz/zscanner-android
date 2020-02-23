package cz.ikem.dci.zscanner.screen_jobs

import android.app.Application
import androidx.lifecycle.*
import cz.ikem.dci.zscanner.persistence.Department
import cz.ikem.dci.zscanner.persistence.Repositories

class DepartmentViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = DepartmentViewModel::class.java.simpleName

    val storedDepartments: LiveData<List<Department>> = Repositories(application).departmentRepository.allDepartments
    var chosenDepartment: MutableLiveData<Department> = MutableLiveData()
}