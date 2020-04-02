package cz.ikem.dci.zscanner

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.teskalabs.seacat.SeaCat
import cz.ikem.dci.zscanner.biometrics.BiometricsKey
import cz.ikem.dci.zscanner.webservices.HttpClient
import java.util.concurrent.Executors

class ZScannerApplication : Application() {

    lateinit var seacat: SeaCat

    lateinit var masterKey: BiometricsKey

    override fun onCreate() {
        super.onCreate()

        val configuration = Configuration.Builder()
            // Defines a thread pool with N threads.
            // Ideally you would choose a number that is dynamic based on the number of cores on the device.
            .setExecutor(Executors.newFixedThreadPool(2))
            .build()

        HttpClient.application = this
        seacat = SeaCat(this, getString(R.string.seacat_api_url))
        WorkManager.initialize(this, configuration)

        // Initialize biometrics lock
        masterKey = BiometricsKey(BIOMETRIC_KEY_NAME)

    }

}
