package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.ZScannerApplication
import java.util.concurrent.Callable

class SplashFragment : androidx.fragment.app.Fragment() {

    val mainHandler = Handler(Looper.getMainLooper())
    var dialogShown = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.removeCallbacksAndMessages(null) // Clear everything
        periodicCheck()
    }

    override fun onDetach() {
        super.onDetach()
        mainHandler.removeCallbacksAndMessages(null) // Clear everything
    }

    private fun periodicCheck() {
        val app = context?.applicationContext as ZScannerApplication

        // Periodically check if the SeaCat is ready, if yes, then make a progress

        // Generate the master key, if not present
        if (app.masterKey.keyPair == null) {
            app.seacat.executorService.submit(Callable {
                app.masterKey.generateKeyPair()
            })
        }

        mainHandler.post(object : Runnable {
            override fun run() {
                if (checkIfReady(app)) {
                    (activity as SplashLoginActivity?)?.makeProgress()
                } else {

                    val biometricsState = BiometricManager.from(app).canAuthenticate()
                    if ((biometricsState != BIOMETRIC_SUCCESS) && !dialogShown) {
                        FaileBiometryDialogFragment(biometricsState, object : Runnable {
                            override fun run() {
                                dialogShown = false
                            }
                        }).show(fragmentManager!!, "failedBiometry")
                        dialogShown = true
                    }

                    mainHandler.postDelayed(this, 1000)
                }
            }
        })

    }

}

fun checkIfReady(app: ZScannerApplication): Boolean {
    if (BiometricManager.from(app).canAuthenticate() != BIOMETRIC_SUCCESS) return false
    if (app.seacat.identity.certificate == null) return false;
    if (app.masterKey.keyPair == null) return false;
    return true
}
