package cz.ikem.dci.zscanner

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import cz.ikem.dci.zscanner.webservices.HttpClient.application

class FireBaseLogger {

    /**
     * Initializes FireBase
     */
    fun initialize() {
        try {
            FirebaseApp.initializeApp(application)
        } catch (e: Exception) {
            Log.d(TAG,"Can’t initialize FireBase: $e")
        }
    }

    /**
     * Send event to FireBase console
     */
    @SuppressLint("MissingPermission")
    fun logEvent(eventName: String, eventValue: String) {
        val sharedPreferences = application.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(PREF_USERNAME, "unknown")
        try {
            val bundle = Bundle()
            val firebaseAnalytics = FirebaseAnalytics.getInstance(application)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, eventValue)
            bundle.putString(USER_ID, username)
            firebaseAnalytics.logEvent(eventName, bundle)
        } catch (e: Exception) {
            Log.d(TAG, "FireBase not initialized: $e")
        }
    }

    companion object {
        const val TAG = "FireBaseLogger"
        const val USER_ID = "user_id"
    }
}
