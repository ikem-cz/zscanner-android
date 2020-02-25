package cz.ikem.dci.zscanner.biometry

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import java.util.*


final class Biometrics(val context: Context, val uiHandler: Handler) {

    val fingerprintManager = FingerprintManagerCompat.from(context)
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    var delegate: Delegate? = null

    private val TAG = Biometrics::class.java.simpleName

    /**
     * <h1> Biometry state</h1>
     *
     * <p> This class has four enums namely: </p>
     * - READY
     * - FINGERPRINTS_NOT_ENROLLED
     * - DEVICE_NOT_SUPPORTED
     * - PERMISSION_NOT_GRANTED
     * - KEYGUARD_NOT_SECURE
     * @see State
     */
    enum class State {
        /** Everything is alright */
        READY,
        /** Device sensor has no fingerprints. */
        FINGERPRINTS_NOT_ENROLLED,
        /** Device has no biometry sensor. */
        DEVICE_NOT_SUPPORTED,
        /** Access to biometry is not granted. */
        PERMISSION_NOT_GRANTED,
        /** Device is not safe. */
        KEYGUARD_NOT_SECURE
    }

    fun getBiometryState(): State {
        return if (!fingerprintManager.isHardwareDetected()) {
            // Device doesn't support fingerprint authentication
            State.DEVICE_NOT_SUPPORTED
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            State.PERMISSION_NOT_GRANTED
        } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            State.PERMISSION_NOT_GRANTED
        } else if (!keyguardManager.isKeyguardSecure()) {
            State.KEYGUARD_NOT_SECURE
        } else if (!fingerprintManager.hasEnrolledFingerprints()) {
            // User hasn't enrolled any fingerprints to authenticate with
            State.FINGERPRINTS_NOT_ENROLLED
        } else {
            // Everything is ready for fingerprint authentication
            State.READY
        }
    }

    /**
     * Proceed with biometric authentification.
     *
     * @throws Error if biometric is not possible.
     */
    fun displayBiometricPrompt(cryptoObject: FingerprintManagerCompat.CryptoObject, callback: (cryptoObject: FingerprintManagerCompat.CryptoObject?) -> Unit) {
        val taskIdentifier = UUID.randomUUID()
        if (getBiometryState() != State.READY) {
            Log.e(TAG, "Biometry is not possible")
            return // TODO: Rather raise the error to prevent a dead-lock
        }

        val cancelSignal = CancellationSignal()
        uiHandler.post {
            delegate?.onAuthenticationBegin(taskIdentifier, cancelSignal)
        }

        fingerprintManager.authenticate(cryptoObject, 0, cancelSignal, object: FingerprintManagerCompat.AuthenticationCallback() {

            override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                Log.w(TAG, "FingerprintManagerCompat.authenticate -> onAuthenticationHelp: " + helpString)
                super.onAuthenticationHelp(helpMsgId, helpString)
                callback(null)
                uiHandler.post {
                    delegate?.onAuthenticationHelp(taskIdentifier, helpMsgId, helpString)
                }
            }
            override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                Log.e(TAG, "FingerprintManagerCompat.authenticate -> onAuthenticationError: " + errString)
                super.onAuthenticationError(errMsgId, errString)
                callback(null)
                uiHandler.post {
                    delegate?.onAuthenticationError(taskIdentifier, errMsgId, errString)
                }

            }
            override fun onAuthenticationFailed() {
                Log.w(TAG, "FingerprintManagerCompat.authenticate -> onAuthenticationFailed" )
                super.onAuthenticationFailed()
                callback(null)
                uiHandler.post {
                    delegate?.onAuthenticationFailed(taskIdentifier)
                }
            }
            override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                result?.cryptoObject?.let {
                    callback(it)
                }
                super.onAuthenticationSucceeded(result)
                uiHandler.post {
                    delegate?.onAuthenticationSucceeded(taskIdentifier, result)
                }
            }
        }, null)
    }

    interface Delegate {
        fun onAuthenticationBegin(taskIdentifier: UUID, cancellationSignal: CancellationSignal)
        fun onAuthenticationHelp(taskIdentifier: UUID, helpMsgId: Int, helpString: CharSequence?)
        fun onAuthenticationError(taskIdentifier: UUID, errMsgId: Int, errString: CharSequence?)
        fun onAuthenticationFailed(taskIdentifier: UUID)
        fun onAuthenticationSucceeded(taskIdentifier: UUID, result: FingerprintManagerCompat.AuthenticationResult?)
    }
}
