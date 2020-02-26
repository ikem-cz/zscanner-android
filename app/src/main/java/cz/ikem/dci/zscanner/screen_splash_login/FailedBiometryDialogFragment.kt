package cz.ikem.dci.zscanner.screen_splash_login

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.teskalabs.seacat.biometrics.Biometrics


class FaileBiometryDialogFragment(biometricsState: Biometrics.State, val dismissCallback: Runnable) : DialogFragment() {

    private val TITLE = "Chyba"
    private val MESSAGE = "Zařízení není připraveno pro biometrii."

    private val KEY_TITLE = "ERR"
    private val KEY_MESSAGE = "MSG"

    init {

        val detail = when (biometricsState) {
            Biometrics.State.READY -> ":-("
            Biometrics.State.FINGERPRINTS_NOT_ENROLLED -> "Nejsou k dispozici otisky prstu."
            Biometrics.State.DEVICE_NOT_SUPPORTED -> "Zarizeni nepodporuje otisky prstu."
            Biometrics.State.PERMISSION_NOT_GRANTED -> "Nedostatek systemovych opravneni."
            Biometrics.State.KEYGUARD_NOT_SECURE -> "Keyguard neni zabezpecen."
        }

        val args = Bundle()
        args.putString(KEY_TITLE, TITLE)
        args.putString(KEY_MESSAGE, MESSAGE + "\n" + detail)
        arguments = args
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissCallback.run()
    }
}
