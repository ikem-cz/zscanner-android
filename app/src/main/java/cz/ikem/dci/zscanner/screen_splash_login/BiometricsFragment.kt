package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teskalabs.seacat.SeaCat
import cz.ikem.dci.zscanner.webservices.HttpClient
import java.nio.ByteBuffer
import androidx.biometric.BiometricPrompt
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.biometrics.BiometricKeyDecrypt

class BiometricsFragment(val app: ZScannerApplication) : androidx.fragment.app.Fragment() {

    private val TAG = BiometricsFragment::class.java.simpleName

    val mainHandler = Handler(Looper.getMainLooper())
    lateinit var fragmentView: View
    var decryptor: BiometricKeyDecrypt? = null
    var cyphertext_len: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView = inflater.inflate(R.layout.fragment_biometrics, container, false)
        displayPrompt()
        return fragmentView
    }

    private fun displayPrompt() {
        val sharedPreferences = app.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val access_token = sharedPreferences.getString(PREF_ACCESS_TOKEN, null)!!
        val cyphertext = ByteBuffer.wrap(Base64.decode(access_token, Base64.DEFAULT))
        cyphertext_len  = cyphertext.limit()

        decryptor = app.masterKey.decrypt(cyphertext)
        val biometrics_prompt = createBiometricPrompt(app.seacat)
        if (biometrics_prompt == null) {
            Log.e(TAG, "Failed to create a biometrics prompt")
        } else {
            decryptor?.prompt(biometrics_prompt, createPromptInfo())
        }
    }


    override fun onResume() {
        super.onResume()
        if (decryptor == null) {
            displayPrompt()
        }
    }

    override fun onPause() {
        super.onPause()
        decryptor = null
    }


    fun createBiometricPrompt(seaCat: SeaCat): BiometricPrompt? {

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                val plaintext = ByteBuffer.allocate(cyphertext_len)
                val decrypted = decryptor?.final(result.cryptoObject, plaintext) ?: false

                if (decrypted) {
                    val plaintext_array = ByteArray(plaintext.limit())
                    plaintext.get(plaintext_array, 0, plaintext.limit())
                    HttpClient.reset(plaintext_array)
                    (activity as SplashLoginActivity?)?.makeProgress()
                } else {
                    HttpClient.reset(null)
                    makeProgressWithDelay()
                }

                decryptor = null
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Log.w(TAG, "Authentication failed $errorCode :: $errString")
                    if (decryptor != null) makeProgressWithDelay() // Display again
                } else {
                    val sharedPreferences = app.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    sharedPreferences.edit()
                        .remove(PREF_USERNAME)
                        .remove(PREF_ACCESS_TOKEN)
                        .apply()
                    (activity as SplashLoginActivity?)?.makeProgress()
                }
                decryptor = null
            }

        }

        return BiometricPrompt(this, seaCat.executorService, callback)
    }


    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val title = app.resources.getString(R.string.login_with_fingerprint)
        val negativeButton = app.resources.getString(R.string.login_with_password)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                // Authenticate without requiring the user to press a "confirm"
                // button after satisfying the biometric check
                .setConfirmationRequired(false)
                .setNegativeButtonText(negativeButton)
                .build()
        return promptInfo
    }

    // We want to retry with a delay, it feels better for an user
    private fun makeProgressWithDelay() {
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                (activity as SplashLoginActivity?)?.makeProgress()
            }
        }, 1000)
    }
}
