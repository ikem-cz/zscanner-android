package cz.ikem.dci.zscanner

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.teskalabs.seacat.android.client.SeaCatClient
import java.util.concurrent.Executors

class ZScannerApplication : Application() {
    override fun onCreate() {

        val configuration = Configuration.Builder()
                // Defines a thread pool with 10 threads.
                // Ideally you would choose a number that is dynamic based on the number
                // of cores on the device.
                .setExecutor(Executors.newFixedThreadPool(2))
                .build()

        WorkManager.initialize(this, configuration)

        // Enable SeaCat
        SeaCatClient.initialize(applicationContext) {
        }
        super.onCreate()
    }


}