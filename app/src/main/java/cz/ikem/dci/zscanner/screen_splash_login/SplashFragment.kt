package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ikem.dci.zscanner.ZScannerApplication
import cz.ikem.dci.zscanner.R

class SplashFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onResume() {
        super.onResume()

        val app = context?.applicationContext as ZScannerApplication

        // Periodically check if the SeaCat is ready, if yes, then make a progress
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if (app.seacat.identity.certificate == null) {
                    mainHandler.postDelayed(this, 1000)
                } else {
                    // SeaCat Identity is ready ... make a progress
                    (activity as SplashLoginActivity?)?.makeProgess()
                }
            }
        })
    }

}
