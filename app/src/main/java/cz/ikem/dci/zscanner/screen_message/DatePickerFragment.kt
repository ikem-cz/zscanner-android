package cz.ikem.dci.zscanner.screen_message

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.lifecycle.ViewModelProviders
import java.util.*


class DatePickerFragment : androidx.fragment.app.DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var mViewModel: CreateMessageViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mViewModel = ViewModelProviders.of(activity!!).get(CreateMessageViewModel::class.java)

        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        c.time = mViewModel.dateTime.value!!
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of TimePickerDialog and return it
        return DatePickerDialog(context, this, year, month, day)

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // mViewModel.dateTime.postValue( Date(year, month, dayOfMonth) )

        val c = Calendar.getInstance()
        c.time = mViewModel.dateTime.value!!
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val min = c.get(Calendar.MINUTE)

        c.set(year, month, dayOfMonth, hour, min)
        val millis = c.timeInMillis

        mViewModel.dateSelected = true
        mViewModel.dateTime.postValue(Date(millis))
    }

}