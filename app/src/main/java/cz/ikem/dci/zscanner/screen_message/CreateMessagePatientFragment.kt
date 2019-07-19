package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import cz.ikem.dci.zscanner.KeyboardCallback
import cz.ikem.dci.zscanner.MruSelectionCallback
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Mru
import cz.ikem.dci.zscanner.webservices.Patient
import kotlinx.android.synthetic.main.fragment_message_patient.view.*

class CreateMessagePatientFragment : Fragment(), Step, MruSelectionCallback {

    private val TAG = CreateMessagePatientFragment::class.java.simpleName

    private lateinit var mViewModel: CreateMessageViewModel

    private var listener: OnCreateMessageViewsInteractionListener? = null
    private var kbCallback: KeyboardCallback? = null

    private var mValidated = false

    override fun onSelected() {
        val viewModel = ViewModelProviders.of(activity!!).get(CreateMessageViewModel::class.java)
        viewModel.currentStep = ModeDispatcher(mViewModel.mode).stepNumberFor(this)
        return
    }

    override fun verifyStep(): VerificationError? {
        return if (!mValidated) {
            VerificationError(getString(R.string.err_invalid_patient))
        } else {
            null
        }
    }

    override fun onError(error: VerificationError) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(activity!!).get(CreateMessageViewModel::class.java)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_message_patient, container, false)

        view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))

        // setup scan button
        view.scan_barcode_layout.setOnClickListener {
            listener?.onScanPatientIdButtonPress()
        }

        view.scan_barcode_fab.setOnClickListener {
            listener?.onScanPatientIdButtonPress()
        }

        view.fab_next_step_1.setOnClickListener {
            listener?.onProceedButtonPress()
        }

        //
        val autoCompleteTextView = view.patient_id_edittext
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (mViewModel.patientInput.value!!.patientText != p0.toString()) {
                    mViewModel.patientInput.value = CreateMessageViewModel.PatientInput(null, p0.toString(), null, true)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        // run validation on patientInput change
        mViewModel.patientInput.observe(this, Observer<CreateMessageViewModel.PatientInput> {
            if (it.patientText != autoCompleteTextView.text.toString()) {
                autoCompleteTextView.setText(it.patientText, it.suggest)
                if (!it.suggest) {
                    autoCompleteTextView.dismissDropDown()
                }
            }
        })

        // setup autocompletion suggestions callbacks
        activity?.applicationContext?.let {
            val adapter = PatientAdapter(it, mViewModel)
            autoCompleteTextView.apply {
                setAdapter(adapter)
                threshold = 3
                // on dismiss suggestions
                setOnDismissListener { view.too_many_layout.visibility = View.INVISIBLE }
                // on suggestion selected
                setOnItemClickListener { _, _, position, _ ->
                    val acceptedSuggestion = adapter.getItem(position)
                    mViewModel.patientInput.value = CreateMessageViewModel.PatientInput(acceptedSuggestion, acceptedSuggestion.getDisplay(), null, false)
                    kbCallback?.hideKeyboard()
                    setText(acceptedSuggestion.getDisplay(), false)
                }
                inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
        }

        mViewModel.patientInput.observe(this, Observer {
            if (it.code != null) {
                mViewModel.startDecodeJob(it.code)
                mValidated = false
                view.patient_validated_layout.visibility = View.INVISIBLE
                view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
            } else if (it.patientObject != null) {
                mValidated = true
                view.patient_validated_layout.visibility = View.VISIBLE
                view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            } else {
                mValidated = false
                view.patient_validated_layout.visibility = View.INVISIBLE
                view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
            }
        })

        // show/hide too many results text
        mViewModel.tooManySuggestions.observe(this, Observer<Boolean> { value ->
            when (value) {
                false -> view.too_many_layout.visibility = View.INVISIBLE
                true -> view.too_many_layout.visibility = View.VISIBLE
            }
        })

        mViewModel.loadingSuggestions.observe(this, Observer<Boolean> { value ->
            when (value) {
                true -> view.progress_bar.visibility = View.VISIBLE
                false -> view.progress_bar.visibility = View.INVISIBLE
            }
        })

        mViewModel.noSuggestions.observe(this, Observer<Boolean> { value ->
            when (value) {
                true -> view.no_patient_layout.visibility = View.VISIBLE
                false -> view.no_patient_layout.visibility = View.INVISIBLE
            }
        })

        val mruAdapter = MruAdapter(this)
        view.mru_recyclerview.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = mruAdapter
        }

        mViewModel.mrus.observe(this, Observer<List<Mru>> {
            mruAdapter.items = it
            mruAdapter.notifyDataSetChanged()
        })

        return view
    }

    override fun onMruSelected(mru: Mru) {
        val patient = Patient(mru.bid!!, mru.name, mru.zid!!)
        mViewModel.patientInput.value = CreateMessageViewModel.PatientInput(patient, patient.getDisplay(), null, false)
        kbCallback?.hideKeyboard()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateMessageViewsInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCreateMessageViewsInteractionListener")
        }
        if (context is KeyboardCallback) {
            kbCallback = context
        } else {
            throw RuntimeException("$context must implement KeyboardCallback")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        kbCallback = null
    }

}