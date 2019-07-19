package cz.ikem.dci.zscanner.screen_splash_login

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.JsonObject
import com.teskalabs.seacat.android.client.CSR
import cz.ikem.dci.zscanner.ACTION_LOGIN_FAILED
import cz.ikem.dci.zscanner.ACTION_LOGIN_OK
import cz.ikem.dci.zscanner.webservices.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

object SeaCatAuthenticator {

    private val TAG = SeaCatAuthenticator::class.java.simpleName

    fun startLogin(username: String, password: String, context: Context) {

        val parentJob = Job()
        val coroutineContext: CoroutineContext = parentJob + Dispatchers.Main
        val scope = CoroutineScope(coroutineContext)

        scope.launch(Dispatchers.IO) {
            Thread.sleep(2000)
            val intent = Intent()
            intent.action = ACTION_LOGIN_OK
            context.sendBroadcast(intent)
        }
    }

}