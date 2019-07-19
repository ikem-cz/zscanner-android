package cz.ikem.dci.zscanner.screen_splash_login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.teskalabs.seacat.android.client.SeaCatClient


class SeaCatBroadcastReceiver(val callback: SeaCatCallback) : BroadcastReceiver() {

    private val TAG = SeaCatBroadcastReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasCategory(SeaCatClient.CATEGORY_SEACAT)) {
            val action = intent.action
            if (action == SeaCatClient.ACTION_SEACAT_STATE_CHANGED) {
                onStateChanged(context, intent.getStringExtra(SeaCatClient.EXTRA_STATE))
            } else if (action == SeaCatClient.ACTION_SEACAT_CLIENTID_CHANGED) {
                onClientIdChanged(context, intent.getStringExtra(SeaCatClient.EXTRA_CLIENT_ID), intent.getStringExtra(SeaCatClient.EXTRA_CLIENT_TAG))
            }
        } else {
            Log.w(TAG, "Unexpected intent: $intent")
        }
    }

    @Synchronized
    private fun onStateChanged(context: Context, state: String) {
        Log.d(TAG, "SeaCatClient state: '$state'")
        // lets fake seacat ready, whatever real state is
        callback.OnSeaCatReady()
    }

    private fun onClientIdChanged(context: Context, clientId: String, clientTag: String) {
        Log.d(TAG, "SeaCatClient tag changed to: '$clientTag'")
        callback.OnClientIdChanged()
    }

    interface SeaCatCallback {
        fun OnSeaCatReady()
        fun OnSeaCatIPConnected()
        fun OnSeaCatEstabilished()
        fun OnClientIdChanged()
    }

}