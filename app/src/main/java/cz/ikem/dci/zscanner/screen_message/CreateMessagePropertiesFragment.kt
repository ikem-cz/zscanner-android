package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Type
import kotlinx.android.synthetic.main.fragment_message_properties.*
import kotlinx.android.synthetic.main.fragment_message_properties.view.*


class CreateMessagePropertiesFragment : Fragment(), Step {

    private lateinit var mViewModel: CreateMessageViewModel
//    private var kbCallback: KeyboardCallback? = null

    private var listener: OnCreateMessageViewsInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.let{ _activity ->
            mViewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)
        }
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_properties, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_next_step_2.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))

        fab_next_step_2.setOnClickListener {
            listener?.onProceedButtonPress()
        }

        mViewModel.types.observe(viewLifecycleOwner, Observer<List<Type>> {
            activity?.let { _activity ->
                val adapter = TypesAdapter(_activity, it)
                type_dropdown?.apply {
                    setAdapter(adapter)
                    setOnItemClickListener { _, _, position, _ ->
                        val item = adapter.getItem(position)
                        mViewModel.type.postValue(item.display)
                        type_dropdown.setText(item.display)
                    }
                }
            }})

        name_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                mViewModel.name.postValue(s.toString())
            }
        })
        
        mViewModel.type.observe(viewLifecycleOwner, Observer {
            updateNextButton()
        })
    }


    override fun onSelected() {
        activity?.let{ _activity ->
            val viewModel = ViewModelProviders.of(_activity).get(CreateMessageViewModel::class.java)
            viewModel.currentStep = ModeDispatcher().stepNumberFor(this)
        }
    }


    override fun verifyStep(): VerificationError? {
        if (mViewModel.type.value!! == "") {
            return VerificationError(getString(R.string.err_no_entrytype))
        }
        return null
    }


    override fun onError(error: VerificationError) {}


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCreateMessageViewsInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCreateMessageViewsInteractionListener")
        }
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun updateNextButton() {
        if (mViewModel.type.value != "") {
            view?.fab_next_step_2?.apply {
                this.isEnabled = true
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            }
        } else {
            view?.fab_next_step_2?.apply {
                this.isEnabled = false
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
            }
        }
    }
}
