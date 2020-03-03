package cz.ikem.dci.zscanner.webservices

import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.ZScannerApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object HttpClient {

    private var mApiServiceBackend: BackendHttpServiceInterface? = null
    lateinit var application: ZScannerApplication

    var accessToken: String? = null // In-memory access token, proof that the user is authenticated

    fun reset(accessToken:  ByteArray?) {
        synchronized(this) {
            mApiServiceBackend = null

            if (accessToken == null) {
                this.accessToken = null
            } else {
                this.accessToken = accessToken.toString(Charsets.UTF_8)
            }

        }
    }

    val ApiServiceBackend: BackendHttpServiceInterface
        get() {
            synchronized(this) {
                val asb = mApiServiceBackend
                if (asb == null) {
                    val url = application.getString(R.string.backend_url)

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
                        .baseUrl(url)
                        .build()

                    val asb2 = retrofit.create(BackendHttpServiceInterface::class.java)
                    mApiServiceBackend = asb2
                    return asb2
                } else {
                    return asb
                }
            }
        }
}


class HeaderInterceptor(val accessToken: String?): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (accessToken != null) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        }

        return chain.proceed(request)
    }
}
