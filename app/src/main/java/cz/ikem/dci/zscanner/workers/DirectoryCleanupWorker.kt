package cz.ikem.dci.zscanner.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import cz.ikem.dci.zscanner.KEY_DIRECTORY
import java.io.File

class DirectoryCleanupWorker(ctx: Context, workerParams: WorkerParameters) : Worker(ctx, workerParams) {

    private val TAG = DirectoryCleanupWorker::class.java.simpleName

    override fun doWork(): Result {

        val path = inputData.getString(KEY_DIRECTORY)!!

        return try {
            val file = File(path)
            deleteRecursive(file)
            Log.d(TAG, "Deleted dir: ${path}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up folder: ${e}")
            Result.failure()
        }
    }
}

fun deleteRecursive(fileOrDirectory: File) {
    if (fileOrDirectory.isDirectory)
        for (child in fileOrDirectory.listFiles())
            deleteRecursive(child)

    fileOrDirectory.delete()
}