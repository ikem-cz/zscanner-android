package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import cz.ikem.dci.zscanner.PREF_ACCESS_TOKEN
import cz.ikem.dci.zscanner.PREF_USERNAME
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.SHARED_PREF_KEY
import cz.ikem.dci.zscanner.webservices.HttpClient
import kotlinx.android.synthetic.main.fragment_login.view.*
import okhttp3.ResponseBody

class LoginFragment : androidx.fragment.app.Fragment(), retrofit2.Callback<ResponseBody> {

    var sharedPreferences: SharedPreferences? = null
    lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView  = inflater.inflate(R.layout.fragment_login, container, false)

        val applicationContext = context?.applicationContext
        if (applicationContext != null) {
            sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        }

        fragmentView.submit_button.setOnClickListener {
            val username = fragmentView.username_edit_text.text.toString()
            val password = fragmentView.password_edit_text.text.toString()

            HttpClient().getApiServiceBackend(context!!).postLogin(
                username,
                password
            ).enqueue(this)

            fragmentView.submit_button.visibility = INVISIBLE
            fragmentView.login_progress_bar.visibility = VISIBLE
        }

        return fragmentView
    }

    // Retrofit callback for a failure
    override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
        fragmentView.login_progress_bar.visibility = INVISIBLE
        fragmentView.submit_button.visibility = VISIBLE

        FailedLoginDialogFragment().showAlert(this)
    }

    // Retrofit callback for a successful HTTP call
    override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {

        if (response.code() == 200) {
            val access_token = response.body()?.bytes()

            if (access_token != null) {
                val sharedPreferences = sharedPreferences
                if (sharedPreferences != null) {
                    val username = fragmentView.username_edit_text.text.toString()
                    sharedPreferences.edit()
                        .putString(PREF_ACCESS_TOKEN, Base64.encodeToString(access_token, Base64.DEFAULT))
                        .putString(PREF_USERNAME, username)
                        .apply()
                    (activity as SplashLoginActivity?)?.makeProgess()
                    return
                }
            }
        }

        fragmentView.login_progress_bar.visibility = INVISIBLE
        fragmentView.submit_button.visibility = VISIBLE

        FailedLoginDialogFragment().showAlert(this)
    }
}
