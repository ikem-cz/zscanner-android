package cz.ikem.dci.zscanner.screen_splash_login

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.fragment.app.DialogFragment

class FaileBiometryDialogFragment(biometricsState: Int, val dismissCallback: Runnable) : DialogFragment() {

    private val TITLE = "Chyba"
    private val MESSAGE = "Zařízení není připraveno pro biometrii."

    val title: String
    val message: String

    init {
        val detail = when (biometricsState) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Zarizeni neni pripraveno, zkuste to pozdeji."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Biometricke overeni neni nastaveno."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Zarizeni nepodporuje biometricke overeni uzivatele."
            else -> ":-("
        }

        title = TITLE
        message = MESSAGE + "\n" + detail
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
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
