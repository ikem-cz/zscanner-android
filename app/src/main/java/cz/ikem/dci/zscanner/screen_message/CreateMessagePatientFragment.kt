package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import cz.ikem.dci.zscanner.KeyboardCallback
import cz.ikem.dci.zscanner.MruSelectionCallback
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Mru
import cz.ikem.dci.zscanner.webservices.Patient
import kotlinx.android.synthetic.main.activity_create_message.*
import kotlinx.android.synthetic.main.fragment_message_patient.*
import kotlinx.android.synthetic.main.fragment_message_patient.view.*


class CreateMessagePatientFragment : Fragment(), Step, MruSelectionCallback {

    private val TAG = CreateMessagePatientFragment::class.java.simpleName

    private lateinit var mViewModel: CreateMessageViewModel

    private var listener: OnCreateMessageViewsInteractionListener? = null
    private var kbCallback: KeyboardCallback? = null

    private var mValidated = false

    override fun onSelected() {
        activity?.let{ _activity ->
            val viewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)
           //TODO viewModel.currentStep = ModeDispatcher().stepNumberFor(this)
        }
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
        activity?.let{ _activity ->
            mViewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)
        }
        super.onCreate(savedInstanceState)
    }



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_patient, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))

        // setup scan button
        scan_barcode_layout.setOnClickListener {
            listener?.onScanPatientIdButtonPress()
        }

        scan_barcode_fab.setOnClickListener {
            listener?.onScanPatientIdButtonPress()
        }

        fab_next_step_1.setOnClickListener {
            val action = CreateMessagePatientFragmentDirections.actionCreateMessagePatientFragmentToCreateMessagePagesFragment()
            findNavController().navigate(action)

        }

        patient_id_edittext?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (mViewModel.patientInput.value?.patientText != p0.toString()) {
                    mViewModel.patientInput.value = CreateMessageViewModel.PatientInput(null, p0.toString(), null, true)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        // run validation on patientInput change
        mViewModel.patientInput.observe(viewLifecycleOwner, Observer<CreateMessageViewModel.PatientInput> { patientInput ->
            if (patientInput.patientText != patient_id_edittext.text.toString().trim()) {
                patient_id_edittext.setText(patientInput.patientText, patientInput.suggest)
                if (!patientInput.suggest) {
                    patient_id_edittext.dismissDropDown()
                }
            }
        })

        // setup autocompletion suggestions callbacks
        activity?.applicationContext?.let {_context ->
            val adapter = PatientAdapter(_context, mViewModel)
            patient_id_edittext.apply {
                setAdapter(adapter)
                // show suggestions after 9 characters
                threshold = 9
                // on dismiss suggestions
                setOnDismissListener { view.too_many_layout?.visibility = View.INVISIBLE }
                // on suggestion selected
                setOnItemClickListener { _, _, position, _ ->
                    val acceptedSuggestion = adapter.getItem(position)
                    mViewModel.patientInput.value = CreateMessageViewModel.PatientInput(acceptedSuggestion, acceptedSuggestion.getDisplay(), null, false)
                    kbCallback?.hideKeyboard()
                    setText(acceptedSuggestion.getDisplay(), false)
                }
            }
        }

        mViewModel.patientInput.observe(viewLifecycleOwner, Observer { patientInput ->
            when {
                patientInput.code != null -> {
                    mViewModel.startDecodeJob(patientInput.code)
                    mValidated = false
                    view.patient_validated_layout.visibility = View.INVISIBLE
                    view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
                }
                patientInput.patientObject != null -> {
                    mValidated = true
                    view.patient_validated_layout.visibility = View.VISIBLE
                    view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
                }
                else -> {
                    mValidated = false
                    view.patient_validated_layout.visibility = View.INVISIBLE
                    view.fab_next_step_1.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
                }
            }
        })

        // show/hide too many results text
        mViewModel.tooManySuggestions.observe(viewLifecycleOwner, Observer<Boolean> { value ->
            when (value) {
                true -> view.too_many_layout.visibility = View.VISIBLE
                false -> view.too_many_layout.visibility = View.INVISIBLE
            }
        })

        mViewModel.loadingSuggestions.observe(viewLifecycleOwner, Observer<Boolean> { value ->
            when (value) {
                true -> view.progress_bar.visibility = View.VISIBLE
                false -> view.progress_bar.visibility = View.INVISIBLE
            }
        })

        mViewModel.noSuggestions.observe(viewLifecycleOwner, Observer<Boolean> { value ->
            when (value) {
                true -> view.no_patient_layout.visibility = View.VISIBLE
                false -> view.no_patient_layout.visibility = View.INVISIBLE
            }
        })

        val mruAdapter = MruAdapter(this)
        mru_recyclerview.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = mruAdapter
        }

        mViewModel.mrus.observe(viewLifecycleOwner, Observer<List<Mru>> {
            mruAdapter.items = it
            mruAdapter.notifyDataSetChanged()
        })
    }

    override fun onMruSelected(mru: Mru) {
        val patient = Patient(mru.externalId!!, mru.name, mru.internalId!!)
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