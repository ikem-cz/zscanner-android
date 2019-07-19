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

    init {

    }

    fun startLogin(username: String, password: String, context: Context) {

        val parentJob = Job()
        val coroutineContext: CoroutineContext = parentJob + Dispatchers.Main
        val scope = CoroutineScope(coroutineContext)

        scope.launch(Dispatchers.IO) {

            try {
                Log.d(TAG, "SeaCatAuthenticator started")
                val csrToken = UUID.randomUUID().toString()
                sendUsername(username, csrToken)
                sendPassword(password, csrToken)

                Log.d(TAG, "Credentials sent, waiting for status check ...")
                var retries = 0

                while (retries < 30) {
                    Log.d(TAG, "SeaCat authenticator pass ${retries}")
                    val status = checkStatus(csrToken)
                    if (status) {
                        val intent = Intent()
                        intent.action = ACTION_LOGIN_OK
                        context.sendBroadcast(intent)
                        Log.d(TAG, "Login OK")
                        return@launch
                    }
                    Thread.sleep(2000)
                    retries++
                    Log.d(TAG, "Login check next pass ..")
                }
                val intent = Intent()
                intent.action = ACTION_LOGIN_FAILED
                context.sendBroadcast(intent)
                Log.d(TAG, "Login FAILED")
            } catch (e: Exception) {
                e.printStackTrace()
                val intent = Intent()
                intent.action = ACTION_LOGIN_FAILED
                context.sendBroadcast(intent)
                Log.d(TAG, "Login EXCEPTION")
            }
        }
    }

    private fun checkStatus(csr: String): Boolean {
        try {
            val json = JsonObject()
            json.addProperty("token", csr)
            val res = HttpClient().getApiServiceAuth().getStatus(json).execute()
            Log.d(TAG, res.toString())
            Log.d(TAG, res.body().toString())

            if (res.body()!!.getAsJsonObject("status").getAsJsonPrimitive("cert").asBoolean == true) {
                return true
            }
            if ((res.body()!!.getAsJsonObject("status").getAsJsonPrimitive("cert").asBoolean == false)
                    && (res.body()!!.getAsJsonObject("status").getAsJsonPrimitive("username").asBoolean == false)
                    && (res.body()!!.getAsJsonObject("status").getAsJsonPrimitive("password").asBoolean == false)) {
                throw Exception("Login failed")
            }
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }


    private fun sendUsername(username: String, csrToken: String) {
        val csr = CSR()
        csr.givenName = username
        csr.uniqueIdentifier = csrToken
        try {
            csr.submit()
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    private fun sendPassword(password: String, csrToken: String) {
        try {
            val json = JsonObject()
            json.addProperty("password", password)
            json.addProperty("token", csrToken)
            HttpClient().getApiServiceAuth().postPassword(json).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.toString())
            throw e
        }
    }

}