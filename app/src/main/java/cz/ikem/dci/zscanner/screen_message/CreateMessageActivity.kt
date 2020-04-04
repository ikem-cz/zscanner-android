package cz.ikem.dci.zscanner.screen_message

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.work.*
import com.google.zxing.integration.android.IntentIntegrator
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Department
import cz.ikem.dci.zscanner.persistence.DocumentType
import cz.ikem.dci.zscanner.screen_message.CreateMessageTypeFragment.Companion.EXTRA_DEPARTMENT
import cz.ikem.dci.zscanner.workers.RefreshDocumentTypesWorker
import kotlinx.android.synthetic.main.page_row.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class CreateMessageActivity : AppCompatActivity(), OnCreateMessageViewsInteractionListener, KeyboardCallback {

    //region constants
    private val TAG = CreateMessageActivity::class.java.simpleName

    //endregion

    private lateinit var mViewModel: CreateMessageViewModel
    private var mCurrentPhotoPath: String? = null // on photo capture result contains file uri

    lateinit var department: Department

    override fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.findViewById<View>(android.R.id.content).windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_message)

        department = (intent.extras?.getSerializable(KEY_DEPARTMENT) as? Department)
                ?: throw Exception()

        mViewModel = ViewModelProviders.of(this, CreateMessageViewModelFactory(application)).get(CreateMessageViewModel::class.java)

        mViewModel.storedTypes.observe(this, Observer<List<DocumentType>> {
            // "Force types field observation for later synchronous access
            Log.v(TAG, "Types observed")
        })

        val actionbar = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        mViewModel.title.observe(this, Observer {
            supportActionBar?.title = it
        })



        mViewModel.department.postValue(department)
        val departmentId = department.id

        val departmentData = Data.Builder()
        //Add department as parameter in Data class.
        departmentData.putString(EXTRA_DEPARTMENT, departmentId)

        // enqueue refresh types worker
        val workManager = WorkManager.getInstance()
        workManager.pruneWork()
        workManager.beginUniqueWork(
                WORKTAG_REFRESH_DOCUMENT_TYPES,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(RefreshDocumentTypesWorker::class.java)
                        .setConstraints(
                                Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                        .setInputData(departmentData.build())
                        .build())
                .enqueue()

//        Log.d(TAG, "Url = ${HttpClient.ApiServiceBackend.getDocumentTypes(departmentId).request().url()}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // return from barcode scan
        if (requestCode == REQUEST_CODE_BARCODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Log.v(TAG, "Barcode scanning cancelled")
                } else {
                    Log.v(TAG, "Scanned: ${result.contents}")
                    mViewModel.patientInput.postValue(CreateMessageViewModel.PatientInput(null, result.contents, result.contents, true))
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else if ((requestCode == REQUEST_CODE_PHOTO) && (resultCode == Activity.RESULT_OK)) {
            val photoNote = note_to_photo?.text.toString().trim()
            mCurrentPhotoPath?.let {
                addPhotoToViewModel(it, photoNote)
            }
        } else {
            //if (resultCode == Activity.RESULT_CANCELED) Toast.makeText(this, "Přerušeno uživatelem", Toast.LENGTH_SHORT).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    //region OnCreateMessageViewsInteractionListener implementation
    override fun onScanPatientIdButtonPress() {
        IntentIntegrator(this).apply {
            setPrompt(applicationContext.resources.getString(R.string.fragment_patient_scan_barcode))
            initiateScan()
        }
    }

    override fun onCapturePagePhotoButtonPress() {
        startPhotoCapture()
    }

    private fun onBack() {
        if (findNavController(R.id.nav_host_fragment).currentDestination?.label != CreateMessagePatientFragment.TAG) {
            findNavController(R.id.nav_host_fragment).navigateUp()
        } else {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.finish_prompt_text))
                .setNegativeButton(getString(R.string.finish_prompt_button_negative)) { _, _ ->
                    mViewModel.type.postValue(null)
                    finish()
                }
                    .setNeutralButton(getString(R.string.no_i_dont_want_to_button), null)
                .show()
        }
        mViewModel.undoAction.postValue(null)
    }

    override fun onBackPressed() {
        onBack()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    //region photo utility functions
    private fun addPhotoToViewModel(path: String, photoNote: String?) {

        mCurrentPhotoPath?.let {
            mViewModel.addPage(path, note = photoNote)
        }
    }

    private fun startPhotoCapture() {
        if (hasPhotoPermissions()) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) { // if intent receiver available
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                    Log.e(TAG, e.message ?: "Exception: memory couldn't be allocated")
                    Toast.makeText(this, R.string.error_out_of_memory , Toast.LENGTH_LONG).show()
                }
                if (photoFile != null) {
                    val photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CODE_PHOTO)
                }
            } else {
                Toast.makeText(this, R.string.error_no_camera, Toast.LENGTH_LONG).show()
            }
        } else {
            requestPhotoPermissions()
        }
    }

    // creates temporary file and sets mCurrentPhotoPath to full path
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss_SS").format(Date())
        val imageFileName = "JPEG_${timeStamp}_${UUID.randomUUID()}"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/" + mViewModel.correlationId)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        mCurrentPhotoPath = image.absolutePath
        return image
    }
    //endregion

    // region android permissions handling
    private fun hasPhotoPermissions(): Boolean {
        return ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
    }

    private fun requestPhotoPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { e -> e == PackageManager.PERMISSION_GRANTED }) {
                startPhotoCapture()
            } else {
                Toast.makeText(this, R.string.error_no_permissions, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    //endregion

}
