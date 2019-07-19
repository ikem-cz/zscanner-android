package cz.ikem.dci.zscanner.webservices

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HttpClient {

    fun getApiServiceBackend(): BackendHttpServiceInterface {
        return Companion.getApiServiceBackend()
    }

    fun getApiServiceAuth(): AuthHttpServiceInterface {
        return Companion.getApiServiceAuth()
    }


    companion object {

        private var mApiServiceBackend: BackendHttpServiceInterface? = null
        private var mApiServiceAuth: AuthHttpServiceInterface? = null

        private fun getApiServiceBackend(): BackendHttpServiceInterface {
            synchronized(this) {
                if (mApiServiceBackend == null) {
                    val client = OkHttpClient.Builder()
                            .build()
                    val retrofit = Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .baseUrl("http://172.16.6.17:10805").build()
                    mApiServiceBackend = retrofit.create(BackendHttpServiceInterface::class.java)
                }
                return mApiServiceBackend!!
            }
        }

        private fun getApiServiceAuth(): AuthHttpServiceInterface {
            synchronized(this) {
                if (mApiServiceAuth == null) {
                    val client = OkHttpClient.Builder()
                            .addInterceptor(SeaCatInterceptor())
                            .callTimeout(500, TimeUnit.MILLISECONDS)
                            .build()
                    val retrofit = Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(client)
                            .baseUrl("http://auth.ikem.seacat").build()
                    mApiServiceAuth = retrofit.create(AuthHttpServiceInterface::class.java)
                }
                return mApiServiceAuth!!
            }
        }
    }
}