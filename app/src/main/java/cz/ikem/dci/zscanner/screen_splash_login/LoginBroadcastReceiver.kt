package cz.ikem.dci.zscanner.screen_splash_login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import cz.ikem.dci.zscanner.ACTION_LOGIN_FAILED
import cz.ikem.dci.zscanner.ACTION_LOGIN_OK

class LoginBroadcastReceiver(val callback: LoginCallback) : BroadcastReceiver() {

    private val TAG = LoginBroadcastReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent ${intent.action}")
        if (intent.action == ACTION_LOGIN_FAILED) {
            callback.OnLoginFailed()
        } else if (intent.action == ACTION_LOGIN_OK) {
            callback.OnLoginOk()
        } else {
            Log.d(TAG, "Received unknown broadcast ${intent.action}")
        }
    }

    interface LoginCallback {
        fun OnLoginOk()
        fun OnLoginFailed()
    }
}