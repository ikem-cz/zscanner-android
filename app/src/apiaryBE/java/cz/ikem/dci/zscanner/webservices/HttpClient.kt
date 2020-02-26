package cz.ikem.dci.zscanner.webservices

import android.content.Context
import cz.ikem.dci.zscanner.ZScannerApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object HttpClient {

    private var mApiServiceBackend: BackendHttpServiceInterface? = null
    lateinit var application: ZScannerApplication

    fun reset() {
        synchronized(this) {
            mApiServiceBackend = null
        }
    }

    val ApiServiceBackend: BackendHttpServiceInterface
        get() {
            synchronized(this) {
                val asb = mApiServiceBackend
                if (asb == null) {
                    val accessToken = application.accessToken

                    val client = OkHttpClient.Builder()
                        .sslSocketFactory(
                            application.seacat.sslContext.socketFactory,
                            application.seacat.trustManager
                        )
                        .addInterceptor(HeaderInterceptor(accessToken))
                        .build()

                    val retrofit = Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .baseUrl("https://zscanner.seacat.io").build()
                    val asb2 = retrofit.create(BackendHttpServiceInterface::class.java)
                    mApiServiceBackend = asb2
                    return asb2
                } else {
                    return asb
                }
            }
        }
}


class HeaderInterceptor(val accessToken: ByteArray?): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (accessToken != null) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken.toString(Charsets.UTF_8))
                .build();
        }

        return chain.proceed(request)
    }
}
