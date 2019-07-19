package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.KEY_PAGE_FILE
import cz.ikem.dci.zscanner.Utils
import java.io.File

class PageFilesCleanupWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    private val TAG = PageFilesCleanupWorker::class.java.simpleName

    override fun doWork(): Result {

        val path = inputData.getString(KEY_PAGE_FILE)!!

        try {

            // remove original file
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                Log.i(TAG, "Deleted ${path} - ${deleted}")
            }

            // remove cached thumbnail if present
            val digest = Utils.digest(path)
            val directory = applicationContext.cacheDir
            val thumbFile = File(directory, digest)
            if (thumbFile.exists()) {
                val deleted = thumbFile.delete()
                Log.i(TAG, "Deleted cached thumb for ${path} - ${deleted}")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up: ${e}")
            return Result.failure()
        }
    }
}