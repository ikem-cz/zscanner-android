package cz.ikem.dci.zscanner.screen_jobs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.work.*
import com.teskalabs.seacat.android.client.SeaCatClient
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.screen_about.AboutActivity
import cz.ikem.dci.zscanner.screen_message.CreateMessageActivity
import cz.ikem.dci.zscanner.screen_splash_login.SplashLoginActivity
import cz.ikem.dci.zscanner.workers.RefreshDocumentTypesWorker
import kotlinx.android.synthetic.main.activity_jobs_overview.*
import java.util.concurrent.TimeUnit


class JobsOverviewActivity : AppCompatActivity() {

    private val TAG = JobsOverviewActivity::class.java.simpleName

    private lateinit var sharedPreferences: SharedPreferences

    private var isFabMenuOpen = false

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

        resetFabMenu()

        new_entry_fab.setOnClickListener {
            if (!isFabMenuOpen) {
                openFabMenu()
            } else {
                closeFabMenu()
            }
        }

        popup_layout_photo.setOnClickListener {
            launchActivity(CREATE_MESSAGE_MODE_PHOTO)
        }

        popup_fab_photo.setOnClickListener {
            launchActivity(CREATE_MESSAGE_MODE_PHOTO)
        }

        popup_layout_exam.setOnClickListener {
            launchActivity(CREATE_MESSAGE_MODE_EXAM)
        }

        popup_fab_exam.setOnClickListener {
            launchActivity(CREATE_MESSAGE_MODE_EXAM)
        }

        popup_layout_doc.setOnClickListener {
            launchActivity(CREATE_MESSAGE_MODE_DOCUMENT)
        }

        popup_fab_doc.setOnClickListener {
            launchActivity(CREATE_MESSAGE_MODE_DOCUMENT)
        }

        val workManager = WorkManager.getInstance()
        workManager.pruneWork()

        // enqueue refresh document types worker
        workManager.beginUniqueWork(
                WORKTAG_REFRESH_DOCUMENT_TYPES,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequest.Builder(RefreshDocumentTypesWorker::class.java)
                        .setConstraints(
                                Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                        .build())
                .enqueue()

        sharedPreferences = application.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(PREF_USERNAME, "")

        nav_view.getHeaderView(0).findViewById<TextView>(R.id.username_textview)?.apply {
            text = getString(R.string.username_prefix)+": "+username.toLowerCase()
        }

        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_purge -> {
                    JobUtils(this).clearFinishedJobs()
                }
                R.id.menu_logout -> {
                    AlertDialog.Builder(this)
                            .setMessage(getString(R.string.logout_prompt_text))
                            .setNegativeButton(getString(R.string.logout_prompt_button_pos)) { _, _ ->
                                JobUtils(this).nukeAllJobs()
                                val sharedPreferences = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
                                sharedPreferences.edit().putBoolean(PREF_LOGGED_IN, false).apply()
                                SeaCatClient.reset()
                                val intent = Intent(this, SplashLoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
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
            }

            it.isChecked = true
            Handler().postDelayed({
                it.isChecked = false
            }, 500)
            drawer_layout.closeDrawers()
            true
        }


    }

    private fun launchActivity(mode:String) {
        val intent = Intent(this, CreateMessageActivity::class.java).apply {
            putExtras(
                    Bundle().apply {
                        putString(CREATE_MESSAGE_MODE_KEY, mode)
                    }
            )
        }
        startActivity(intent)
    }

    private fun getPopupList() = listOf<View>(popup_layout_doc, popup_layout_exam, popup_layout_photo)

    private fun resetFabMenu() {
        closeFabMenu(false)
    }

    private fun closeFabMenu(animate: Boolean = true) {
        isFabMenuOpen = false
        new_entry_fab.setImageDrawable(resources.getDrawable(R.drawable.ic_plus2))
        new_entry_fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        if (false) {
            popup_layout_buttons.animate()
                .setInterpolator(AccelerateInterpolator())
                .setDuration(150)
                .translationY(88f)
        } else {
            popup_layout_buttons.getLayoutParams().height= getResources().getDimension(R.dimen.fab_group_min).toInt()
            popup_layout_buttons.requestLayout()
        }
        /*
        getPopupList().forEach {
            if (animate) {
                it.animate()
                        .setInterpolator(AccelerateInterpolator())
                        .setDuration(150)
                        .translationY(0f)
                it.animate()
                        .alpha(0f)
                        .withEndAction {
                            it.visibility = View.GONE
                        }
            } else {
                it.visibility = View.GONE
                it.translationY = 0f
            }
        }
        */
    }

    private fun openFabMenu() {
        isFabMenuOpen = true
        new_entry_fab.setImageDrawable(resources.getDrawable(R.drawable.ic_close))
        new_entry_fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.invalid)));
        popup_layout_buttons.getLayoutParams().height= LinearLayout.LayoutParams.WRAP_CONTENT
        popup_layout_buttons.requestLayout()
        /*
        getPopupList().forEachIndexed { index, view ->
            view.apply {
                visibility = View.VISIBLE
                alpha = 0f
                animate().alpha(1f)
                animate()
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(150)
                        .translationY(-when (index) {
                            0 -> resources.getDimension(R.dimen.fab_expanding_y)
                            1 -> resources.getDimension(R.dimen.fab_expanding_y2)
                            2 -> resources.getDimension(R.dimen.fab_expanding_y3)
                            else -> throw Exception("Expectation failed")
                        })
            }

        }
        */

        /*if (Utils.tutorialNextStep(2, this)) {
            Handler().postDelayed({
                try {
                    Utils.makeTooltip("Obrázky pořízené aplikací lze zařadit do různých kategorií Zlatokopa.\n\nNa zkoušku zkusíme pořídit fotografii.\n\nKlepněte na ikonu foťáčku.", photo_popup_fab, Gravity.TOP, this) {
                        Utils.tutorialAdvance(this)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }, 250)
        }*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        resetFabMenu()
    }

    override fun onPostResume() {
        super.onPostResume()
        val prefs = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val firstTimePromptShown = prefs.getBoolean(PREF_FIRST_TIME_PROMPTED, false)
        /*if (!firstTimePromptShown) {
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

}

