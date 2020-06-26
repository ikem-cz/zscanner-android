package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.biometrics.BiometricsKey
import cz.ikem.dci.zscanner.webservices.HttpClient
import kotlinx.android.synthetic.main.fragment_login.view.*
import okhttp3.ResponseBody
import java.nio.ByteBuffer

//TODO: If (BiometricManager.from(app).canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS), display the info message about that

class LoginFragment : androidx.fragment.app.Fragment(), retrofit2.Callback<ResponseBody> {

    var sharedPreferences: SharedPreferences? = null
    lateinit var fragmentView: View
    private val TAG = LoginFragment::class.java.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView  = inflater.inflate(R.layout.fragment_login, container, false)

        val applicationContext = context?.applicationContext
        if (applicationContext != null) {
            sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        }

        fragmentView.submit_button.setOnClickListener {
            val username = fragmentView.username_edit_text.text.toString()
            val password = fragmentView.password_edit_text.text.toString()

            HttpClient.ApiServiceBackend.postLogin(
                username,
                password
            ).enqueue(this)

            fragmentView.submit_button.visibility = INVISIBLE
            fragmentView.login_progress_bar.visibility = VISIBLE
        }

        fragmentView.username_edit_text.requestFocus()

        return fragmentView
    }

    // Retrofit callback for a failure
    override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
        fragmentView.login_progress_bar.visibility = INVISIBLE
        fragmentView.submit_button.visibility = VISIBLE

        FailedLoginDialogFragment().show(fragmentManager!!, "failedLogin")
    }

    // Retrofit callback for a successful HTTP call
    override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {

        if (response.code() == 200) {
            val access_token = response.body()?.bytes()

            if (access_token != null) {

                val app = context?.applicationContext as ZScannerApplication
                val cyphertext = ByteBuffer.allocate(access_token.size + 4096)

                // Try to encrypt the token and save it in shared preferences
                if (app.masterKey.encrypt(ByteBuffer.wrap(access_token), cyphertext)) {
                    Log.d(TAG, "Token encryption success")
                    val cyphertext_array = ByteArray(cyphertext.limit())
                    cyphertext.get(cyphertext_array)

                    val sharedPreferences = sharedPreferences
                    if (sharedPreferences != null) {
                        val username = fragmentView.username_edit_text.text.toString()
                        sharedPreferences.edit()
                            .putString(PREF_ACCESS_TOKEN, Base64.encodeToString(cyphertext_array, Base64.DEFAULT))
                            .putString(PREF_USERNAME, username)
                            .apply()
                    }
                } else { // encryption failed. Allow to login, but without possibility to use biometry
                    Log.w(TAG, "Token encryption failed")
                    Toast.makeText(context, getString(R.string.no_biometry_available), Toast.LENGTH_SHORT).show()
                }

                HttpClient.reset(access_token)
                (activity as SplashLoginActivity?)?.makeProgress()
                return
            }
        }

        fragmentView.login_progress_bar.visibility = INVISIBLE
        fragmentView.submit_button.visibility = VISIBLE

        FailedLoginDialogFragment().show(fragmentManager!!, "failedLogin")
    }
}
