package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ikem.dci.zscanner.R
import kotlinx.android.synthetic.main.fragment_login.view.*

class LoginFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        view.submit_button.setOnClickListener {
            val username = view.username_edit_text.text.toString()
            val password = view.password_edit_text.text.toString()
            val activity = activity
            if ((activity != null) && (activity is LoginFragmentCallback)) {
                activity.submitCredentials(username, password)
            }
        }
        return view
    }

    interface LoginFragmentCallback {
        fun submitCredentials(username: String, password: String)
    }

}
