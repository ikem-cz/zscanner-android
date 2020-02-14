package cz.ikem.dci.zscanner.persistence

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class Repositories(val app: Context) {

    private val initializationScope: Lazy<CoroutineScope>
        get() = lazy {
            CoroutineScope(Job() + Dispatchers.IO)
        }

    val typeRepository: TypeRepository
    val jobsRepository: SendJobRepository
    val mruRepository: MruRepository
    val departmentRepository: DepartmentRepository

    init {

        val documentTypeDao = TypeDatabase.getDatabase(app, initializationScope).documentTypeDao()
        typeRepository = TypeRepository(documentTypeDao)

        val jobsDao = SendJobDatabase.getDatabase(app).sendJobDao()
        jobsRepository = SendJobRepository(jobsDao)

        val mruDao = MruDatabase.getDatabase(app, initializationScope).mruDao()
        mruRepository = MruRepository(mruDao)

        val departmentsDao = DepartmentDatabase.getDatabase(app).departmentDao()
        departmentRepository = DepartmentRepository(departmentsDao)

    }

}