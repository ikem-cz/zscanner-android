package cz.ikem.dci.zscanner.screen_message

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.lifecycle.ViewModelProviders
import java.util.*


class TimePickerFragment : androidx.fragment.app.DialogFragment(), TimePickerDialog.OnTimeSetListener {


    private lateinit var mViewModel: CreateMessageViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mViewModel = ViewModelProviders.of(activity!!).get(CreateMessageViewModel::class.java)

        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        c.time = mViewModel.dateTime.value!!
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val min = c.get(Calendar.MINUTE)
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(context, this, hour, min, true)

    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        val c = Calendar.getInstance()
        c.time = mViewModel.dateTime.value!!
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        c.set(year, month, day, hourOfDay, minute)
        val millis = c.timeInMillis

        mViewModel.timeSelected = true
        mViewModel.dateTime.postValue(Date(millis))


    }
}