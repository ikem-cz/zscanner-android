package cz.ikem.dci.zscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import cz.ikem.dci.zscanner.screen_jobs.DepartmentsUtils
import cz.ikem.dci.zscanner.screen_jobs.JobUtils
import cz.ikem.dci.zscanner.screen_splash_login.SplashLoginActivity
import cz.ikem.dci.zscanner.webservices.HttpClient


class LogoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if(intent.action == BROADCAST_ACTION_LOGOUT){
            Toast.makeText(context, context.resources.getString(R.string.automatic_log_out_notification), Toast.LENGTH_LONG).show()
            Log.v(TAG, "Logout due to end of shift. Time: ${System.currentTimeMillis()}")

            logout(context)
        }
    }

    private fun logout(context: Context){
        HttpClient.reset(null)

        DepartmentsUtils(context).nukeAllDepartments()
        JobUtils(context).nukeAllJobs()

        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .remove(PREF_USERNAME)
                .remove(PREF_ACCESS_TOKEN)
                .apply()

        val intent = Intent(context, SplashLoginActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(context, intent, null)
    }

    companion object{
        const val TAG = "LogoutReceiver"
    }
}