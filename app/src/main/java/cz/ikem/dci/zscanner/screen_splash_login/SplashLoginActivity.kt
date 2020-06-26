package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import cz.ikem.dci.zscanner.*
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
        val accessToken = sharedPreferences.getString(PREF_ACCESS_TOKEN, null)
        val inMemoryToken = HttpClient.accessToken

        when {
            // If the application is not ready, show the splash screen with a progress bar spinning
            !checkIfReady(app) -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, SplashFragment())
                        .commit()

            }
            // If we don't have an access token in shared preferences or in-memory -> go to login fragment
            accessToken == null && inMemoryToken == null -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LoginFragment())
                        .commit()
            }
            // If we do have sharedPrefs token, but no in-memory HttpClient access token, get it
            inMemoryToken == null && (BiometricManager.from(app).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, BiometricsFragment(app))
                        .commit()

            }
            // If biometry is not available, fallback to a username/password login dialog
            inMemoryToken == null && (BiometricManager.from(app).canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, LoginFragment())
                        .commit()

            }
            inMemoryToken != null -> {
                // We are done here ...
                val intent = Intent(this, JobsOverviewActivity::class.java)
                startActivity(intent)
                this.finish()
            }
            else -> {
                Toast.makeText(app, getString(R.string.error_login_not_successful), Toast.LENGTH_LONG).show()
            }
        }
    }
}
