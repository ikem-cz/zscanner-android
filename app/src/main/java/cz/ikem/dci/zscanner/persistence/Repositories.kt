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

    val docTypeRepository: DocumentTypeRepository
    val docSubTypeRepository: DocumentSubTypeRepository
    val jobsRepository: SendJobRepository
    val mruRepository: MruRepository
    val departmentRepository: DepartmentRepository

    init {

        val documentTypeDao = DocumentTypeDatabase.getDatabase(app, initializationScope).documentTypeDao()
        docTypeRepository = DocumentTypeRepository(documentTypeDao)

        val documentSubTypeDao = DocumentSubTypeDatabase.getDatabase(app, initializationScope).documentSubTypeDao()
        docSubTypeRepository = DocumentSubTypeRepository(documentSubTypeDao)

        val jobsDao = SendJobDatabase.getDatabase(app).sendJobDao()
        jobsRepository = SendJobRepository(jobsDao)

        val mruDao = MruDatabase.getDatabase(app, initializationScope).mruDao()
        mruRepository = MruRepository(mruDao)

        val departmentsDao = DepartmentDatabase.getDatabase(app).departmentDao()
        departmentRepository = DepartmentRepository(departmentsDao)

    }

}