package cz.ikem.dci.zscanner.webservices

import android.content.Context
import cz.ikem.dci.zscanner.ZScannerApplication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HttpClient {

    fun getApiServiceBackend(context: Context): BackendHttpServiceInterface {
        return Companion.getApiServiceBackend(context)
    }

    companion object {

        private var mApiServiceBackend: BackendHttpServiceInterface? = null

        private fun getApiServiceBackend(context: Context): BackendHttpServiceInterface {
            val application: ZScannerApplication = context.applicationContext as ZScannerApplication

            synchronized(this) {
                if (mApiServiceBackend == null) {
                    val client = OkHttpClient.Builder()
                        .sslSocketFactory(
                            application.seacat.sslContext.socketFactory,
                            application.seacat.trustManager
                        )
                        .build()
                    val retrofit = Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .baseUrl("https://zscanner.seacat.io").build()
                    mApiServiceBackend = retrofit.create(BackendHttpServiceInterface::class.java)
                }
                return mApiServiceBackend!!
            }
        }
    }
}