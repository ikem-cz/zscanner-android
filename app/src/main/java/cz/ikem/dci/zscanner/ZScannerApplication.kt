package cz.ikem.dci.zscanner

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import java.util.concurrent.Executors
import com.teskalabs.seacat.SeaCat

class ZScannerApplication : Application() {

    lateinit var seacat: SeaCat

    override fun onCreate() {
        super.onCreate()

        val configuration = Configuration.Builder()
            // Defines a thread pool with N threads.
            // Ideally you would choose a number that is dynamic based on the number of cores on the device.
            .setExecutor(Executors.newFixedThreadPool(2))
            .build()

        seacat = SeaCat(this, "http://10.0.2.2:8080/seacat/seacat")

        WorkManager.initialize(this, configuration)
    }


}