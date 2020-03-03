package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.work.*
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.screen_about.AboutActivity
import cz.ikem.dci.zscanner.screen_message.CreateMessageActivity
import cz.ikem.dci.zscanner.screen_splash_login.SplashLoginActivity
import cz.ikem.dci.zscanner.webservices.HttpClient
import cz.ikem.dci.zscanner.workers.RefreshDepartmentsWorker
import kotlinx.android.synthetic.main.activity_jobs_overview.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit


class JobsOverviewActivity : AppCompatActivity() {

    private val TAG = JobsOverviewActivity::class.java.simpleName

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs_overview)
        setSupportActionBar(tool_bar)

        val actionbar = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_drawer)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, JobsOverviewFragment())
                    .commitNow()
        }

        val workManager = WorkManager.getInstance()
        workManager.pruneWork()

        // enqueue refresh departments worker
        val refreshDepartments = PeriodicWorkRequest.Builder(RefreshDepartmentsWorker::class.java, 2, TimeUnit.HOURS) // TODO decide how often
                .setConstraints(Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
        workManager.enqueue(refreshDepartments)

        sharedPreferences = application.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(PREF_USERNAME, "")

        nav_view.getHeaderView(0).findViewById<TextView>(R.id.username_textview)?.apply {
            text = getString(R.string.username_prefix)+": " + (username?.toLowerCase() ?: "???")
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_purge -> {
                    JobUtils(this).clearFinishedJobs()
                }
                R.id.menu_logout -> {
                    AlertDialog.Builder(this)
                            .setMessage(getString(R.string.logout_prompt_text))
                            .setNegativeButton(getString(R.string.logout_prompt_button_pos)) { _, _ -> logout() }
                            .setPositiveButton(getString(R.string.logout_prompt_button_neg), null)
                            .show()
                }
                R.id.menu_about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }
                R.id.menu_repeat_tutorial -> {
                    startTutorial()
                }
                R.id.menu_lock -> {
                    lock()
                }
            }

            it.isChecked = true
            Handler().postDelayed({
                it.isChecked = false
            }, 500)
            drawer_layout.closeDrawers()
            true
        }


    }

    //TODO delete?
//    private fun launchCreateMessageActivity() {
//        val intent = Intent(this, CreateMessageActivity::class.java)
//        startActivity(intent)
//    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onPostResume() {
        super.onPostResume()
        /*val prefs = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val firstTimePromptShown = prefs.getBoolean(PREF_FIRST_TIME_PROMPTED, false)
        if (!firstTimePromptShown) {
            prefs.edit().putBoolean(PREF_FIRST_TIME_PROMPTED, true).commit()
            AlertDialog.Builder(this)
                    .setTitle("Zdá se, že aplikaci spouštíte poprvé")
                    .setMessage("Přejete si spustit průvodce?")
                    .setPositiveButton("Ano") { _, _ ->
                        startTutorial()
                    }
                    .setNegativeButton("Ne", null)
                    .show()
        }*/
    }

    private fun startTutorial() {
        Utils.tutorialInitialize(this)
        /*if (Utils.tutorialNextStep(1, this)) {
            Utils.makeTooltip("Vítejte v aplikaci zScanner.\n\nAplikace slouží k rychlému pořízení fotodokumentace pomocí mobilního telefonu.\n\nInformační bubliny Vás nyní provedou typickým použitím aplikace.\n\n(pro další krok vždy klepněte na bublinu)",
                    tool_bar, Gravity.BOTTOM, this, showArrow = false, modal = true) {
                Utils.makeTooltip("Začněte klepnutím sem", new_entry_fab, Gravity.START, this) {
                    Utils.tutorialAdvance(this)
                }
            }
        }*/
    }


    private fun logout() {
        val app = applicationContext as ZScannerApplication
        val access_token = HttpClient.accessToken
        HttpClient.reset(null)

        // Call the logout remotely ... but ignore the result (for now).
        sharedPreferences = application.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)

        HttpClient.ApiServiceBackend.postLogout(access_token).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            }
        })



        JobUtils(this).nukeAllJobs()

        val sharedPreferences = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove(PREF_USERNAME)
            .remove(PREF_ACCESS_TOKEN)
            .apply()

        val intent = Intent(this, SplashLoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun lock() {
        val app = applicationContext as ZScannerApplication
        HttpClient.reset(null)

        val intent = Intent(this, SplashLoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}
