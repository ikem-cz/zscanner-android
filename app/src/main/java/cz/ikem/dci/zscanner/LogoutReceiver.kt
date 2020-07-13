package cz.ikem.dci.zscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class LogoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Alarm Triggered", Toast.LENGTH_LONG).show()
        Log.e("DEBUGGING", "MyAlarmReceiver, onReceive: AAAAAAAAAAAA = MyAlarmReceiver ${System.currentTimeMillis()}")
    }
}