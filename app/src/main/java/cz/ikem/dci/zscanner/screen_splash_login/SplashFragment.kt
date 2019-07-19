package cz.ikem.dci.zscanner.screen_splash_login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.ikem.dci.zscanner.R

class SplashFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = SplashFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }


}
