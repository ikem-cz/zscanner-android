package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cz.ikem.dci.zscanner.R


class FailedLoginDialogFragment : DialogFragment() {

    private val title = getString(R.string.error)
    private val message = getString(R.string.error_login_not_successful)

    init {
        val args = Bundle()
        args.putString(KEY_TITLE, title)
        args.putString(KEY_MESSAGE, message)
        arguments = args
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val args = arguments
        val title = args?.getString(KEY_TITLE, "")
        val message = args?.getString(KEY_MESSAGE, "")

        return AlertDialog.Builder(context!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog?.cancel() }
            .create()
    }

    companion object{
        const val KEY_TITLE = "ERR"
        const val KEY_MESSAGE = "MSG"
    }
}
