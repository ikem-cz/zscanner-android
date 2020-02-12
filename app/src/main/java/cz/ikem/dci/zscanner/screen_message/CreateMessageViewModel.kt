package cz.ikem.dci.zscanner.screen_message

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.persistence.Mru
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.persistence.Type
import cz.ikem.dci.zscanner.screen_jobs.JobUtils
import cz.ikem.dci.zscanner.webservices.HttpClient
import cz.ikem.dci.zscanner.webservices.Patient
import kotlinx.coroutines.*
import java.lang.AssertionError
import java.text.SimpleDateFormat
import java.util.*

class CreateMessageViewModelFactory(private val zapplication: Application, private val mode: CreateMessageMode) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateMessageViewModel(zapplication, mode) as T
    }
}

class CreateMessageViewModel(private val zapplication: Application, val mode: CreateMessageMode) : AndroidViewModel(zapplication) {

    private val TAG = CreateMessageViewModel::class.java.simpleName

    // requests are assembled on Z by correlationId
    val correlationId = UUID.randomUUID().toString()

    private var cleanupHandled: Boolean = false

    // always contains all types -- move elsewhere?
    var types: LiveData<List<Type>> = Repositories(zapplication).typeRepository.allTypes

    var currentStep: Int = 0

    // always contains all mrus -- move elsewhere?
    val mrus: LiveData<List<Mru>> = Repositories(zapplication).mruRepository.mru()

    val type: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }

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
                val response = HttpClient().getApiServiceBackend().decodePatient(code).execute()
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

    val name: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }

    val undoAction: MutableLiveData<PageActionsQueue.PageAction> = MutableLiveData<PageActionsQueue.PageAction>().apply { value = null }

    private val mPageActions: PageActionsQueue = PageActionsQueue()
    val pageActionsQueue: PageActionsQueue
        get() {
            return mPageActions
        }

    val pageActions: MutableLiveData<PageActionsQueue> = MutableLiveData<PageActionsQueue>().apply { value = mPageActions }

    fun addPage(path: String?, position: Int? = -1) {
        if (path == null){
            return
        }
        var _position = position
        if (_position == null){
            _position = -1
        }
        val action = PageActionsQueue.PageAction(PageActionsQueue.Page(path), PageActionsQueue.PageActionType.ADDED, _position)
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
    fun onProcessEnd() {
        val toSend = mPageActions.makePages().map { e -> e.path }
        val toClean = mPageActions.actionsList().asSequence().map { e -> e.page.path }.distinct().filter { e -> !toSend.contains(e) }.toList()
        JobUtils(zapplication).scheduleFilesCleanup(toClean)

        cleanupHandled = true

        val entry = patientInput.value?.patientObject
        handleProcessOutput(entry, mode, type.value, name.value, toSend)

    }

    private fun handleProcessOutput(patient: Patient?, mode: CreateMessageMode, type: String?, name: String?, filePaths: List<String>) {

        // insert mru
        patient?.let{ _patient ->
            MruUtils(getApplication<ZScannerApplication>()).addMru(_patient)
        }

        // set current date and time as string value
        val dateString = SimpleDateFormat("MM/dd/yyyy HH:mm").format(Date())
        val numpages = filePaths.count()

        // create description string
        val description = run {
            val allTypes = types.value
            val modeDisplayString = zapplication.resources.getString(ModeDispatcher(mode).modeNameResource)
            val typesDisplayList = allTypes?.filter { e -> e.mode == mode }
            val typeDisplayString = if (!typesDisplayList.isNullOrEmpty()) {
                " - ${typesDisplayList.filter { it.type == type }[0].display}"
            } else { "" }
            "${modeDisplayString}${typeDisplayString} - $numpages str."
        }

        if (patient == null) {
            Log.e(TAG, "patient is null")
            return
        }

        if (type == null){
            Log.e(TAG, "type is null")
            return
        }

        if (name == null){
            Log.e(TAG, "name is null")
            return
        }

        JobUtils(getApplication<ZScannerApplication>()).addJob(
                correlationId,
                System.currentTimeMillis(),
                patient,
                mode,
                type,
                name,
                dateString,
                filePaths,
                description)
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