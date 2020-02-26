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
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.webservices.HttpClient
import java.nio.ByteBuffer
import androidx.biometric.BiometricPrompt
import cz.ikem.dci.zscanner.PREF_ACCESS_TOKEN
import cz.ikem.dci.zscanner.SHARED_PREF_KEY
import cz.ikem.dci.zscanner.biometrics.BiometricKeyDecrypt

class BiometricsFragment(val app: ZScannerApplication) : androidx.fragment.app.Fragment() {

    private val TAG = BiometricsFragment::class.java.simpleName

    val mainHandler = Handler(Looper.getMainLooper())
    lateinit var fragmentView: View
    var decryptor: BiometricKeyDecrypt? = null
    var cyphertext_len: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView = inflater.inflate(R.layout.fragment_biometrics, container, false)

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

        return fragmentView
    }


    fun createBiometricPrompt(seaCat: SeaCat): BiometricPrompt? {

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                val plaintext = ByteBuffer.allocate(cyphertext_len)
                val decrypted = decryptor?.final(result.cryptoObject, plaintext) ?: false

                if (decrypted) {
                    HttpClient.reset(plaintext.array())
                    (activity as SplashLoginActivity?)?.makeProgess()
                } else {
                    HttpClient.reset(null)
                    makeProgressWithDelay()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Log.w(TAG, "Authentication failed $errorCode :: $errString")
                }
                makeProgressWithDelay()
            }

        }

        return BiometricPrompt(this, seaCat.executorService, callback)
    }


    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("R.string.prompt_info_title")
            .setSubtitle("R.string.prompt_info_subtitle")
            .setDescription("R.string.prompt_info_description")
            // Authenticate without requiring the user to press a "confirm"
            // button after satisfying the biometric check
            .setConfirmationRequired(false)
            .setNegativeButtonText("R.string.prompt_info_use_app_password")
            .build()
        return promptInfo
    }

    // We want to retry with a delay, it feels better for an user
    private fun makeProgressWithDelay() {
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                (activity as SplashLoginActivity?)?.makeProgess()
            }
        }, 1000)
    }
}
