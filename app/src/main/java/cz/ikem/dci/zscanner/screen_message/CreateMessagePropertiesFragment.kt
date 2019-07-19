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
import cz.ikem.dci.zscanner.KeyboardCallback
import cz.ikem.dci.zscanner.OnCreateMessageViewsInteractionListener
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.persistence.Type
import kotlinx.android.synthetic.main.fragment_message_properties.view.*
import java.text.SimpleDateFormat
import java.util.*

class CreateMessagePropertiesFragment : Fragment(), Step {

    private lateinit var mViewModel: CreateMessageViewModel
    private var kbCallback: KeyboardCallback? = null

    override fun onSelected() {
        val viewModel = ViewModelProviders.of(activity!!).get(CreateMessageViewModel::class.java)
        viewModel.currentStep = ModeDispatcher(viewModel.mode).stepNumberFor(this)
        return
    }

    override fun verifyStep(): VerificationError? {
        if (mViewModel.type.value!! == "") {
            return VerificationError(getString(R.string.err_no_entrytype))
        }
        if (!mViewModel.dateSelected || !mViewModel.timeSelected) {
            return VerificationError(getString(R.string.err_no_datetime))
        }
        return null
    }

    override fun onError(error: VerificationError) {}

    private var listener: OnCreateMessageViewsInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        mViewModel = ViewModelProviders.of(activity!!).get(CreateMessageViewModel::class.java)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_message_properties, container, false)

        view.fab_next_step_2.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid));

        view.fab_next_step_2.setOnClickListener {
            listener?.onProceedButtonPress()
        }

        val typeDropDown = view.type_dropdown

        mViewModel.types.observe(this, Observer<List<Type>> {
            val adapter = TypesAdapter(activity!!,it, mViewModel.mode)
            typeDropDown.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    val item = adapter.getItem(position)
                    mViewModel.type.postValue(item.type)
                    typeDropDown.setText(item.display)
                }
            }
        })

        view.name_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                mViewModel.name.postValue(s.toString())
            }
        })

        // date and time pickers
        view.date_button.setOnClickListener {
            DatePickerFragment().show(fragmentManager!!, "datePicker")
        }
        view.time_button.setOnClickListener {
            TimePickerFragment().show(fragmentManager!!, "timePicker")
        }
        mViewModel.dateTime.observe(this, Observer<Date> { value ->
            if (mViewModel.dateSelected) {
                view.date_button.setText(SimpleDateFormat("d.M.yyyy").format(value))
            } else {
                view.date_button.setText("")
            }
            if (mViewModel.timeSelected) {
                view.time_button.setText(SimpleDateFormat("HH:mm").format(value))
            } else {
                view.time_button.setText("")
            }
            updateNextButton()
        })

        mViewModel.type.observe(this, Observer {
            updateNextButton()
        })

        return view
    }

    private fun updateNextButton() {
        if (mViewModel.timeSelected && mViewModel.timeSelected && mViewModel.type.value!! != "") {
            view?.fab_next_step_2?.apply {
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            }
        } else {
            view?.fab_next_step_2?.apply {
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid))
            }
        }
    }

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

}
