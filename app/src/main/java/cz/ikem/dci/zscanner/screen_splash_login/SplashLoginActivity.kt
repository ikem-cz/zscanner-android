package cz.ikem.dci.zscanner.screen_splash_login

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.teskalabs.seacat.android.client.SeaCatClient
import cz.ikem.dci.zscanner.ACTION_LOGIN_FAILED
import cz.ikem.dci.zscanner.ACTION_LOGIN_OK
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.screen_jobs.JobsOverviewActivity
import cz.ikem.dci.zscanner.screen_splash_login.LoginFragment.LoginFragmentCallback
import kotlinx.android.synthetic.main.fragment_login.*


class SplashLoginActivity : AppCompatActivity(), LoginFragmentCallback, LoginBroadcastReceiver.LoginCallback, SeaCatBroadcastReceiver.SeaCatCallback {

    private val TAG = SplashLoginActivity::class.java.simpleName

    private lateinit var mHandler: Handler

    private lateinit var mSeaCatReceiver: SeaCatBroadcastReceiver
    private lateinit var mLoginReceiver: LoginBroadcastReceiver

    private lateinit var mViewModel: LoginViewModel

    private var mProgressBarAnimator: ObjectAnimator? = null
    private var mLoginButtonAnimator: ObjectAnimator? = null

    private var mSplashOnly: Boolean = true// indicate current activity docMode -- either user login or already authenticated, so only display splash

    private var mAlreadyLaunchedNextActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        mSeaCatReceiver = SeaCatBroadcastReceiver(this)
        mLoginReceiver = LoginBroadcastReceiver(this)

        if (savedInstanceState == null) {

            if (mViewModel.loginstate == LoginViewModel.LoginState.LOGGED_IN) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, SplashFragment.newInstance())
                        .commit()
                mSplashOnly = true
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LoginFragment.newInstance())
                        .commit()
                mSplashOnly = false
            }
        }


        mViewModel.state.observe(this, Observer<Triple<LoginViewModel.LoginState?, LoginViewModel.LoginState, LoginViewModel.SeaCatState>> {
            val prevloginstate = it.first
            val loginstate = it.second
            val seacatstate = it.third

            Log.d(TAG, "Observed: $prevloginstate, $loginstate, $seacatstate")

            var skipViewVisibliltyAdjustments = false

            if ((prevloginstate == LoginViewModel.LoginState.SUBMITTED) && (loginstate == LoginViewModel.LoginState.LOGGED_IN)) {
                skipViewVisibliltyAdjustments = true
            }

            if ((loginstate == LoginViewModel.LoginState.LOGGED_IN) && (seacatstate == LoginViewModel.SeaCatState.READY)) {
                if (!mAlreadyLaunchedNextActivity) {
                    mAlreadyLaunchedNextActivity = true
                    skipViewVisibliltyAdjustments = true
                    val intent = Intent(this, JobsOverviewActivity::class.java)
                    startActivity(intent)
                    this.finish()

                }
            }

            if ((!skipViewVisibliltyAdjustments) && (!mSplashOnly)) {

                if (submit_button != null) {
                    submit_button.isEnabled = loginstate != LoginViewModel.LoginState.SUBMITTED
                }

                if ((prevloginstate != LoginViewModel.LoginState.SUBMITTED) && (loginstate == LoginViewModel.LoginState.SUBMITTED)) {

                    //val errortext = findViewById<TextView>(R.bid.errorTextView)
                    //errortext.visibility = View.INVISIBLE

                    var oldalpha = 0f
                    if (mProgressBarAnimator != null) {
                        mProgressBarAnimator!!.pause()
                        oldalpha = mProgressBarAnimator!!.animatedValue as Float
                        mProgressBarAnimator!!.cancel()
                    }
                    val progressbar = login_progress_bar
                    mProgressBarAnimator = ObjectAnimator.ofFloat(progressbar, View.ALPHA, oldalpha, 1f).apply {
                        startDelay = 250
                        duration = 250
                        start()
                    }

                    var oldloginbtnalpha = 1f
                    if (mLoginButtonAnimator != null) {
                        mLoginButtonAnimator!!.pause()
                        oldloginbtnalpha = mLoginButtonAnimator!!.animatedValue as Float
                        mLoginButtonAnimator!!.cancel()
                    }
                    val loginButton = submit_button
                    mLoginButtonAnimator = ObjectAnimator.ofFloat(loginButton, View.ALPHA, oldloginbtnalpha, 0f).apply {
                        duration = 250
                        start()
                    }
                }

                if ((prevloginstate == LoginViewModel.LoginState.SUBMITTED) && (loginstate == LoginViewModel.LoginState.LOGIN_FAILED)) {

                    //val errortext = findViewById<TextView>(R.bid.errorTextView)
                    //errortext.visibility = View.VISIBLE
                    val currentFragment = supportFragmentManager.fragments.last()
                    FailedLoginDialogFragment().showAlert(currentFragment)

                    var oldprogressalpha = 1f
                    if (mProgressBarAnimator != null) {
                        mProgressBarAnimator!!.pause()
                        oldprogressalpha = mProgressBarAnimator!!.animatedValue as Float
                        mProgressBarAnimator!!.cancel()
                    }
                    val progressbar = login_progress_bar
                    mProgressBarAnimator = ObjectAnimator.ofFloat(progressbar, View.ALPHA, oldprogressalpha, 0f).apply {
                        duration = 250
                        start()
                    }

                    var oldloginbtnalpha = 0f
                    if (mLoginButtonAnimator != null) {
                        mLoginButtonAnimator!!.pause()
                        oldloginbtnalpha = mLoginButtonAnimator!!.animatedValue as Float
                        mLoginButtonAnimator!!.cancel()
                    }
                    val loginButton = submit_button
                    mLoginButtonAnimator = ObjectAnimator.ofFloat(loginButton, View.ALPHA, oldloginbtnalpha, 1f).apply {
                        startDelay = 250
                        duration = 250
                        start()
                    }
                }
            }
        })

        val intentFilter = IntentFilter().apply {
            addAction(ACTION_LOGIN_OK)
            addAction(ACTION_LOGIN_FAILED)
        }
        registerReceiver(mLoginReceiver, intentFilter)

        val intentFilterSeaCat = IntentFilter().apply {
            addCategory(SeaCatClient.CATEGORY_SEACAT)
            addAction(SeaCatClient.ACTION_SEACAT_STATE_CHANGED)
            addAction(SeaCatClient.ACTION_SEACAT_CLIENTID_CHANGED)
        }
        registerReceiver(mSeaCatReceiver, intentFilterSeaCat)

        // trigger initial state delivery
        mHandler = Handler()
        mHandler.postDelayed(
                {
                    Log.d(TAG, "Requesting initial SeaCat state broadcast")
                    SeaCatClient.broadcastState()
                }, 500)
    }

    override fun onDestroy() {
        unregisterReceiver(mLoginReceiver)
        unregisterReceiver(mSeaCatReceiver)
        super.onDestroy()
    }

    override fun OnSeaCatIPConnected() {
        mViewModel.seacatstate = LoginViewModel.SeaCatState.IP_CONNECTED
    }

    override fun OnLoginOk() {
        mViewModel.loginstate = LoginViewModel.LoginState.LOGGED_IN
    }

    override fun OnLoginFailed() {
        mViewModel.loginstate = LoginViewModel.LoginState.LOGIN_FAILED
    }

    override fun OnSeaCatReady() {
        mViewModel.seacatstate = LoginViewModel.SeaCatState.READY
    }

    override fun OnClientIdChanged() {
    }

    override fun OnSeaCatEstabilished() {
        mViewModel.seacatstate = LoginViewModel.SeaCatState.ESTABILISHED
    }

    override fun submitCredentials(username: String, password: String) {
        Log.d(TAG, "submitCredentials()")
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.findViewById<View>(android.R.id.content).windowToken, 0)
        if ((username == "") || (password == "")) {
            Toast.makeText(this, "Vyplňte uživatelské jméno a heslo", Toast.LENGTH_SHORT).show()
        } else {
            mViewModel.loginstate = LoginViewModel.LoginState.SUBMITTED
            mViewModel.username = username
            SeaCatAuthenticator.startLogin(username, password, this)
        }
    }
}
