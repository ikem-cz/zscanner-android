package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.ikem.dci.zscanner.PREF_ACCESS_TOKEN
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.SHARED_PREF_KEY
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.screen_jobs.JobsOverviewActivity

class SplashLoginActivity : AppCompatActivity() {

    private val TAG = SplashLoginActivity::class.java.simpleName

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

        // Make a first move
        makeProgess()
    }

    // This method is trying to make a progress in the login/splash state machine
    // with the intention to forward the user to a "main" activity
    fun makeProgess() {
        val app = application as ZScannerApplication

        // If we don't have a SeaCat certificate, wait for that in a splash screen
        if (checkIfReady(app) == false) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SplashFragment())
                .commit()
            return
        }

        // If we don't have an access token, then go to login fragment
        val access_token = sharedPreferences.getString(PREF_ACCESS_TOKEN, null)
        if (access_token == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, LoginFragment())
                .commit()
            return
        }

        // If we don't have the in-memory access token, get it
        if (app.accessToken == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, BiometricsFragment(app, access_token))
                .commit()
            return
        }

        // We are done here ...
        val intent = Intent(this, JobsOverviewActivity::class.java)
        startActivity(intent)
        this.finish()
    }


}
