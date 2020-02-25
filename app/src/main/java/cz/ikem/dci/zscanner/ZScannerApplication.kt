package cz.ikem.dci.zscanner

import android.app.Application
import android.os.Handler
import androidx.work.Configuration
import androidx.work.WorkManager
import com.teskalabs.seacat.SeaCat
import cz.ikem.dci.zscanner.biometry.Biometrics
import cz.ikem.dci.zscanner.biometry.MasterKey
import java.util.concurrent.Executors


class ZScannerApplication : Application() {

    lateinit var seacat: SeaCat

    val uiHandler = Handler()
    lateinit var biometrics: Biometrics
    lateinit var masterKey: MasterKey

    var accessToken: ByteArray? = null // In-memory access token, proof that the user is authenticated

    override fun onCreate() {
        super.onCreate()

        val configuration = Configuration.Builder()
            // Defines a thread pool with N threads.
            // Ideally you would choose a number that is dynamic based on the number of cores on the device.
            .setExecutor(Executors.newFixedThreadPool(2))
            .build()

        seacat = SeaCat(this, "https://zscanner.seacat.io/seacat")

        WorkManager.initialize(this, configuration)

        // Initialize biometrics lock
        biometrics = Biometrics(this, uiHandler)
        masterKey = MasterKey(biometrics)

        if (masterKey.getKeyPair() == null) {
            Thread(Runnable {
                masterKey.generateKeyPair()
            }).start()
        }

    }

}
