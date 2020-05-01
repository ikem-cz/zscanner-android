package cz.ikem.dci.zscanner.screen_message

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.annotations.SerializedName
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.persistence.*
import cz.ikem.dci.zscanner.screen_jobs.JobUtils
import cz.ikem.dci.zscanner.webservices.HttpClient
import cz.ikem.dci.zscanner.webservices.Patient
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class CreateMessageViewModelFactory(private val zapplication: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateMessageViewModel(zapplication) as T
    }
}

class CreateMessageViewModel(private val zapplication: Application) : AndroidViewModel(zapplication) {

    private val TAG = CreateMessageViewModel::class.java.simpleName

    // set the toolbar title for each fragment
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title
    fun updateActionBarTitle(title: String) = _title.postValue(title)

    // requests are assembled on Z by correlationId
    val correlationId = UUID.randomUUID().toString()

    private var cleanupHandled: Boolean = false

    var storedTypes: LiveData<List<DocumentType>> = Repositories(zapplication).docTypeRepository.allDocumentTypes

    // always contains all mrus -- move elsewhere?
    val mrus: LiveData<List<Mru>> = Repositories(zapplication).mruRepository.mru()

    val type: MutableLiveData<DocumentType> = MutableLiveData()
    val subtype: MutableLiveData<DocumentSubType> = MutableLiveData()
    val department: MutableLiveData<Department> = MutableLiveData()
    val additionalNote: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }

    data class PatientInput(
            @SerializedName("patientObject")
            val patientObject: Patient?,
            @SerializedName("patientText")
            val patientText: String,
            @SerializedName("code")
            val code: String?,
            @SerializedName("suggest")
            val suggest: Boolean
    )
    val patientInput: MutableLiveData<PatientInput> = MutableLiveData<PatientInput>().apply { value = PatientInput(null, "", null, false) }

    val loadingSuggestions: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val noSuggestions: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val tooManySuggestions: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val logoutOnHttpResponse: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    val undoAction: MutableLiveData<PageActionsQueue.PageAction> = MutableLiveData<PageActionsQueue.PageAction>().apply { value = null }

    private val mPageActions: PageActionsQueue = PageActionsQueue()
    val pageActionsQueue: PageActionsQueue
        get() {
            return mPageActions
        }

    val pageActions: MutableLiveData<PageActionsQueue> = MutableLiveData<PageActionsQueue>().apply { value = mPageActions }

    fun addPage(path: String?, position: Int? = -1, note: String?) {
        if (path == null){
            return
        }
        var _position = position
        if (_position == null){
            _position = -1
        }
        var _note = note
        if(_note == null){
            _note = ""
        }
        val action = PageActionsQueue.PageAction(PageActionsQueue.Page(path, _note), PageActionsQueue.PageActionType.ADDED, _position)
        mPageActions.add(action)
        undoAction.postValue(null)
        pageActions.postValue(mPageActions)
    }

    fun removeAt(pos: Int) {
        val list = mPageActions.makePages()
        val element = list[pos]
        val action = PageActionsQueue.PageAction(element, PageActionsQueue.PageActionType.REMOVED)
        mPageActions.add(action)
        undoAction.postValue(PageActionsQueue.PageAction(element, PageActionsQueue.PageActionType.ADDED, pos))
        pageActions.postValue(mPageActions)
    }

    fun move(posFrom: Int, posTo: Int) {
        val list = mPageActions.makePages()
        val element = list[posFrom]
        val action = PageActionsQueue.PageAction(element, PageActionsQueue.PageActionType.MOVED, posTo)
        mPageActions.add(action)
        undoAction.postValue(null)
        pageActions.postValue(mPageActions)
    }

    /**
     *  Called when message creating process is complete and no validation errors were encountered
     */
    fun onProcessEnd(completion: (error: Error?) -> Unit) {
        val toSend = mPageActions.makePages()
        val toClean = mPageActions.actionsList().asSequence().map { e -> e.page }.distinct().filter { e -> !toSend.contains(e) }.toList()
        JobUtils(zapplication).scheduleFilesCleanup(toClean)

        cleanupHandled = true

        val docType = type.value
        if (docType == null){
            return completion(Error("document type is null"))
        }

        val docSubType = subtype.value

        val docDepartment = department.value
        if (docDepartment == null){
            Log.e(TAG, "department is null")
            return completion(Error("department is null"))
        }

        val patient = patientInput.value?.patientObject
        if (patient == null) {
            return completion(Error("patient is null"))
        }

        handleProcessOutput(patient, docType, docSubType, docDepartment, additionalNote.value, toSend) { error ->
            if(error != null){
              Log.e(TAG, "CreateMessageViewModel, onProcessEnd: handleProcessOutput, error = $error")
            }
            completion(error)
        }
    }

    private fun handleProcessOutput(patient: Patient, docType: DocumentType, docSubType: DocumentSubType?, docDepartment: Department, additionalNote: String?, filePaths: List<PageActionsQueue.Page>, completion: (error: Error?) -> Unit) {

        // insert mru
        MruUtils(getApplication<ZScannerApplication>()).addMru(patient)

        // set current date and time as string value
        val dateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date())
        val numpages = filePaths.count()

        val descWithSubtype = zapplication.resources.getString(cz.ikem.dci.zscanner.R.string.fragment_jobs_description_with_subtype, docType.display, docSubType?.display, numpages)
        val descNoSubtype = zapplication.resources.getString(cz.ikem.dci.zscanner.R.string.fragment_jobs_description_without_subtype, docType.display, numpages)
        // create description string
        val description = if (!docSubType?.display.isNullOrEmpty()) {
            descWithSubtype
        } else {
            descNoSubtype
        }


        JobUtils(getApplication<ZScannerApplication>()).addJob(
                correlationId,
                System.currentTimeMillis(),
                patient,
                docType.id,
                docSubType?.id ?: "",
                docDepartment.id,
                additionalNote ?: "",
                dateString,
                filePaths,
                description)
        completion(null)
    }

    override fun onCleared() {
        if (!cleanupHandled) {
            val toClean = mPageActions.actionsList().asSequence().map { e -> e.page }.distinct().toList()
            JobUtils(zapplication).scheduleFilesCleanup(toClean, true, correlationId)
        }
        super.onCleared()
    }

    fun containsAtLeastOnePage(): Boolean {
        val pagesCount = mPageActions.makePages().map { e -> e.path }.size
        return pagesCount > 0
    }




}
