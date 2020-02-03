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
import com.google.common.io.ByteStreams
import com.google.zxing.integration.android.IntentIntegrator
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.persistence.Type
import kotlinx.android.synthetic.main.activity_create_message.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateMessageActivity : AppCompatActivity(), OnCreateMessageViewsInteractionListener, StepperLayout.StepperListener, KeyboardCallback {

    //region constants
    private val TAG = CreateMessageActivity::class.java.simpleName

    //endregion

    private lateinit var mViewModel: CreateMessageViewModel
    private lateinit var mMode: CreateMessageMode
    private var mCurrentPhotoPath: String? = null // on photo capture result contains file uri

    override fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.findViewById<View>(android.R.id.content).windowToken, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_message)

        mMode = when (intent.extras?.getString(CREATE_MESSAGE_MODE_KEY)) {
            CREATE_MESSAGE_MODE_DOCUMENT -> CreateMessageMode.DOCUMENT
            CREATE_MESSAGE_MODE_EXAM -> CreateMessageMode.EXAM
            CREATE_MESSAGE_MODE_PHOTO -> CreateMessageMode.PHOTO
            else -> throw Exception()
        }

        mViewModel = ViewModelProviders.of(this, CreateMessageViewModelFactory(application, mMode)).get(CreateMessageViewModel::class.java)

        stepper_layout.adapter = CreateMessageStepAdapter(supportFragmentManager, this, mMode)
        stepper_layout.currentStepPosition = mViewModel.currentStep
        stepper_layout.setListener(this)
        stepper_layout.setShowBottomNavigation(false)


        mViewModel.types.observe(this, Observer<List<Type>> {
            // "Force types field observation for later synchronous access
            Log.v(TAG, "Types observed")
        })

        val actionbar = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
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
                    //mViewModel.patientInput.postValue(CreateMessageViewModel.PatientInput(MaybePatient( result.contents ), true, true))
                    mViewModel.patientInput.postValue( CreateMessageViewModel.PatientInput( null, result.contents, result.contents, false) )
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        } else if ((requestCode == REQUEST_CODE_PHOTO) && (resultCode == Activity.RESULT_OK)) {
            addPhotoToViewModel(mCurrentPhotoPath!!)
        } else if ((requestCode == REQUEST_CODE_PICK_PHOTO) && (resultCode == Activity.RESULT_OK)) {
            // copy selected file to storage dir
            val target = createImageFile()
            val from = this.contentResolver.openInputStream(data!!.data!!)
            val to = FileOutputStream(target)
            ByteStreams.copy(from, to)
            from.close()
            to.flush()
            to.close()
            addPhotoToViewModel(target.absolutePath)
        } else {
            //if (resultCode == Activity.RESULT_CANCELED) Toast.makeText(this, "Přerušeno uživatelem", Toast.LENGTH_SHORT).show()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    //region OnCreateMessageViewsInteractionListener implementation
    override fun onScanPatientIdButtonPress() {
        IntentIntegrator(this).apply {
            setPrompt("Naskenujte čárový kód nebo QR kód s ID pacienta")
            initiateScan()
        }
    }

    override fun onCapturePagePhotoButtonPress() {
        startPhotoCapture()
    }


    override fun onAttachButtonPress() {
        startPickPhoto()
    }


    override fun onProceedButtonPress() {
        stepper_layout.proceed()
    }

    private fun onBack() {
        if (mViewModel.currentStep > 0) {
            stepper_layout.currentStepPosition = mViewModel.currentStep - 1
        } else {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.finish_prompt_text))
                .setNegativeButton(getString(R.string.finish_prompt_button_pos), { _, _ -> finish() })
                .setPositiveButton(getString(R.string.finish_prompt_button_neg), null)
                .show()
        }
        mViewModel.undoAction.postValue( null )
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

    //endregion

    //region StepperListener implementation

    /**
     *  Called when stepper process complete
     */
    override fun onCompleted(completeButton: View?) {
        mViewModel.onProcessEnd()
        finish()
    }

    /**
     *  Called when back button pressed on first step
     */
    override fun onReturn() {
        finish()
    }

    override fun onStepSelected(position: Int) {
        // set appbar title
        supportActionBar?.setTitle(getString(ModeDispatcher(mMode).stepTitleAt(position),getString(ModeDispatcher(mMode).modeNameResource)))
        // hide keyboard on step change
        val view = this.currentFocus
        if (view != null) {
            hideKeyboard()
        }
    }

    /**
     *  Called on step verification error
     */
    override fun onError(verificationError: VerificationError?) {
        // just propagate error message to a toast
        Toast.makeText(this, verificationError?.errorMessage, Toast.LENGTH_SHORT).show()
    }


    //endregion


    //region photo utility functions
    private fun addPhotoToViewModel(path: String) {
        mCurrentPhotoPath?.let {
            mViewModel.addPage(path)
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
                    Toast.makeText(this, "Nezdařila se rezervace prostoru v úložišti telefonu", Toast.LENGTH_LONG).show()
                }
                if (photoFile != null) {
                    val photoURI = FileProvider.getUriForFile(this, "cz.ikem.dci.zscanner.fileprovider", photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CODE_PHOTO)
                }
            } else {
                Toast.makeText(this, "Není k dispozici fotoaparát", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPhotoPermissions()
        }
    }

    private fun startPickPhoto() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/jpeg"
        intent.action = Intent.ACTION_GET_CONTENT
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_PICK_PHOTO)
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
                Toast.makeText(this, "Bez udělení potřebných oprávnění nelze pokračovat", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    //endregion

}
