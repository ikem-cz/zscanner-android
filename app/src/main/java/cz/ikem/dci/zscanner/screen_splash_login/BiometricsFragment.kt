package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ikem.dci.zscanner.R
import cz.ikem.dci.zscanner.ZScannerApplication
import java.nio.ByteBuffer

class BiometricsFragment(val app: ZScannerApplication, val access_token: String) : androidx.fragment.app.Fragment() {

    val mainHandler = Handler(Looper.getMainLooper())
    lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentView = inflater.inflate(R.layout.fragment_biometrics, container, false)

        val cyphertext = ByteBuffer.wrap(Base64.decode(access_token, Base64.DEFAULT))

        Thread(Runnable {
            val plaintext = ByteBuffer.allocate(cyphertext.limit())

            val result = app.masterKey.decrypt(cyphertext, plaintext)
            if (result == 0L) {
                app.accessToken = plaintext.array()
            } else {
                app.accessToken = null
            }

            (activity as SplashLoginActivity?)?.makeProgess()
        }).start()

        return fragmentView
    }
}
