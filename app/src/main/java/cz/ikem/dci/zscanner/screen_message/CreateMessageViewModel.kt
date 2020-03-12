package cz.ikem.dci.zscanner.screen_message

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.persistence.Mru
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.persistence.DocumentSubType
import cz.ikem.dci.zscanner.persistence.DocumentType
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

    // requests are assembled on Z by correlationId
    val correlationId = UUID.randomUUID().toString()

    private var cleanupHandled: Boolean = false

    var storedTypes: LiveData<List<DocumentType>> = Repositories(zapplication).docTypeRepository.allDocumentTypes

    // always contains all mrus -- move elsewhere?
    val mrus: LiveData<List<Mru>> = Repositories(zapplication).mruRepository.mru()

    val type: MutableLiveData<DocumentType> = MutableLiveData()
    val subtype: MutableLiveData<DocumentSubType> = MutableLiveData()
    val additionalNote: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }

    data class PatientInput(
            val patientObject: Patient?,
            val patientText: String,
            val code: String?,
            val suggest: Boolean
    )
    val patientInput: MutableLiveData<PatientInput> = MutableLiveData<PatientInput>().apply { value = PatientInput(null, "", null, false) }

    val loadingSuggestions: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val noSuggestions: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }
    val tooManySuggestions: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = false }

    fun startDecodeJob(code: String) {
        decodeJob?.cancel()
        dispatchDecodeJob(code)
    }

    private var decodeJob: Job? = null
    private fun dispatchDecodeJob(code: String) {
        decodeJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val response = HttpClient.ApiServiceBackend.decodePatient(code).execute()
                if (!isActive) {
                    throw Exception("Cancelled")
                }
                noSuggestions.postValue(false)

                val body = response.body() ?: throw Exception("response.body() was null")
                patientInput.postValue(PatientInput(body, body.getDisplay(), null, false))
            } catch (e: Exception) {
                Log.d(TAG, "Decode job failed for $code")
                noSuggestions.postValue(true)
            }
        }
    }

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
        val toSend = mPageActions.makePages().map { e -> e.path }
        val toClean = mPageActions.actionsList().asSequence().map { e -> e.page.path }.distinct().filter { e -> !toSend.contains(e) }.toList()
        JobUtils(zapplication).scheduleFilesCleanup(toClean)

        cleanupHandled = true

        val docType = type.value
        if (docType == null){
            return completion(Error("document type is null"))
        }

        val docSubType = subtype.value

        val patient = patientInput.value?.patientObject
        if (patient == null) {
            return completion(Error("patient is null"))
        }

        handleProcessOutput(patient, docType, docSubType, additionalNote.value, toSend) { error ->
            Log.e(TAG, "CreateMessageViewModel, onProcessEnd: handleProcessOutput, error = $error")
            completion(error)
        }
    }

    private fun handleProcessOutput(patient: Patient, docType: DocumentType, docSubType: DocumentSubType?, additionalNote: String?, filePaths: List<String>, completion: (error: Error?) -> Unit) {

        // insert mru
        MruUtils(getApplication<ZScannerApplication>()).addMru(patient)

        // set current date and time as string value
        val dateString = SimpleDateFormat("MM/dd/yyyy HH:mm").format(Date())
        val numpages = filePaths.count()

        val descWithSubtype = zapplication.resources.getString(cz.ikem.dci.zscanner.R.string.description_with_subtype, docType.display, docSubType?.display, numpages)
        val descNoSubtype = zapplication.resources.getString(cz.ikem.dci.zscanner.R.string.description_without_subtype, docType.display, numpages)
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
                additionalNote ?: "",
                dateString,
                filePaths,
                description)
        completion(null)
    }

    override fun onCleared() {
        if (!cleanupHandled) {
            val toClean = mPageActions.actionsList().asSequence().map { e -> e.page.path }.distinct().toList()
            JobUtils(zapplication).scheduleFilesCleanup(toClean, true, correlationId)
        }
        super.onCleared()
    }

    fun containsAtLeastOnePage(): Boolean {
        val pagesCount = mPageActions.makePages().map { e -> e.path }.size
        return pagesCount > 0
    }




}