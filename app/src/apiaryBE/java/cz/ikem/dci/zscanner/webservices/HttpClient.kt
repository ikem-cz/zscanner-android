package cz.ikem.dci.zscanner.webservices

import cz.ikem.dci.zscanner.ZScannerApplication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HttpClient {

    fun getApiServiceBackend(application: ZScannerApplication): BackendHttpServiceInterface {
        return Companion.getApiServiceBackend(application)
    }

    fun getApiServiceAuth(): AuthHttpServiceInterface {
        return Companion.getApiServiceAuth()
    }


    companion object {

        private var mApiServiceBackend: BackendHttpServiceInterface? = null
        private var mApiServiceAuth: AuthHttpServiceInterface? = null

        private fun getApiServiceBackend(application: ZScannerApplication): BackendHttpServiceInterface {
            synchronized(this) {
                if (mApiServiceBackend == null) {
                    val client = OkHttpClient.Builder()
                        //TODO: Check with ECR and use "non-deprecated" version of this
                        .sslSocketFactory(application.seacat.sslContext.socketFactory)
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

        //TODO: Remove this code - it is SeaCat 2 related code, now obsolete
        private fun getApiServiceAuth(): AuthHttpServiceInterface {
            synchronized(this) {
                if (mApiServiceAuth == null) {
                    val client = OkHttpClient.Builder()
                            .callTimeout(500, TimeUnit.MILLISECONDS)
                            .build()
                    val retrofit = Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .baseUrl("http://auth.ikem.seacat").build()
                    //.baseUrl("http://10.0.2.2:10805").build()
                    mApiServiceAuth = retrofit.create(AuthHttpServiceInterface::class.java)
                }
                return mApiServiceAuth!!
            }
        }
    }
}