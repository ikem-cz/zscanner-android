package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment


class FailedLoginDialogFragment : DialogFragment() {

    private val TITLE = "Chyba"
    private val MESSAGE = "Přihlášení se nezdařilo"

    private val KEY_TITLE = "ERR"
    private val KEY_MESSAGE = "MSG"

    fun showAlert(targetFragment: Fragment) {
        val dialog = FailedLoginDialogFragment()
        val args = Bundle()
        args.putString(KEY_TITLE, TITLE)
        args.putString(KEY_MESSAGE, MESSAGE)
        dialog.arguments = args
        dialog.setTargetFragment(targetFragment, 0)
        dialog.show(targetFragment.fragmentManager!!, "tag")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val args = arguments
        val title = args!!.getString(KEY_TITLE, "")
        val message = args.getString(KEY_MESSAGE, "")

        return AlertDialog.Builder(context!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog?.cancel() }
            .create()
    }
}