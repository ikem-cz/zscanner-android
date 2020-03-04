package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import cz.ikem.dci.zscanner.PREF_ACCESS_TOKEN
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.SHARED_PREF_KEY
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.screen_jobs.JobsOverviewActivity
import cz.ikem.dci.zscanner.webservices.HttpClient


class SplashLoginActivity : AppCompatActivity() {

    private val TAG = SplashLoginActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

        // Make a first move
        makeProgress()
    }

    // This method is trying to make a progress in the login/splash state machine
    // with the intention to forward the user to a "main" activity
    fun makeProgress() {
        val app = application as ZScannerApplication

        // If the application is not ready, show the splash screen with a progress bar spinning
        if (!checkIfReady(app)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SplashFragment())
                .commit()
            return
        }

        // If we don't have an access token, then go to login fragment
        val accessToken = sharedPreferences.getString(PREF_ACCESS_TOKEN, null)
        if ((accessToken == null) && (HttpClient.accessToken == null)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment())
                .commit()
            return
        }

        // If we don't have the in-memory HttpClient access token, get it
        if (HttpClient.accessToken == null) {
            if (BiometricManager.from(app).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, BiometricsFragment(app))
                    .commitAllowingStateLoss() // otherwise it crashes if user leaves the screen
            } else {
                // If biometry is not available, fallback to a username/password login dialog
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, LoginFragment())
                    .commit()
            }
            return
        }

        // We are done here ...
        val intent = Intent(this, JobsOverviewActivity::class.java)
        startActivity(intent)
        this.finish()
    }

}
