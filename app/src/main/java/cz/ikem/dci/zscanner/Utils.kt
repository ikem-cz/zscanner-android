package cz.ikem.dci.zscanner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.security.MessageDigest

// Name of Notification Channel for verbose notifications of background work

class Utils {

    companion object {

        val TAG = Utils::class.java.simpleName

        private const val VERBOSE_NOTIFICATION_CHANNEL_NAME = "Verbose WorkManager Notifications"
        const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"
        const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
        const val NOTIFICATION_TITLE = "WorkRequest Starting"
        const val NOTIFICATION_ID = 1

        fun digest(x: String): String {
            return hashString("MD5", x)
        }

        private fun hashString(type: String, input: String): String {
            val HEX_CHARS = "0123456789ABCDEF"
            val bytes = MessageDigest
                    .getInstance(type)
                    .digest(input.toByteArray())
            val result = StringBuilder(bytes.size * 2)
            bytes.forEach {
                val i = it.toInt()
                result.append(HEX_CHARS[i shr 4 and 0x0f])
                result.append(HEX_CHARS[i and 0x0f])
            }
            return result.toString()
        }

        // unpacks bitmap from path
        fun unpackBitmap(path: String): Bitmap {

            val targetW = 250
            val targetH = 250

            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, bmOptions)
            val photoW = bmOptions.outWidth
            val photoH = bmOptions.outHeight

            // Determine how much to scale down the image
            val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor
            bmOptions.inPurgeable = true

            val sourceBitmap = BitmapFactory.decodeFile(path, bmOptions)

            // rotate generated bitmap
            val exif = androidx.exifinterface.media.ExifInterface(path)
            val exifRotation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)
            val angle = when (exifRotation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
            val matrix = Matrix()
            matrix.preRotate(angle.toFloat())

            return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
        }

        fun makeStatusNotification(message: String, context: Context) {

            // Make a channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
                val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.description = description

                // Add the channel
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationManager.createNotificationChannel(channel)
            }

            // Create the notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(LongArray(0))

            // Show the notification
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        }

        fun tutorialInitialize(ctx: Context) {
            val sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE)
            sharedPreferences.edit().putInt(PREF_TUTORIAL_NEXT_STEP, 1).commit()
        }

//        fun tutorialNextStep(step: Int, ctx: Context?) : Boolean {
//            if (ctx != null) {
//                val sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE)
//                val nextStep = sharedPreferences.getInt(PREF_TUTORIAL_NEXT_STEP, -1)
//                return (nextStep == step)
//            } else {
//                return false
//            }
//        }
//
//        fun tutorialAdvance(ctx : Context?) {
//            if (ctx != null) {
//                val sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE)
//                val step = sharedPreferences.getInt(PREF_TUTORIAL_NEXT_STEP, -1)
//                sharedPreferences.edit().putInt(PREF_TUTORIAL_NEXT_STEP, step + 1).commit()
//            }
//        }

//        fun makeTooltip(text: String, view: View, gravity: Int, context: Context?, showArrow : Boolean = true, modal : Boolean = false, onDismissCallback: (() -> Unit)? = null) {
//            if (context != null) {
//                SimpleTooltip.Builder(context).apply {
//                    anchorView(view)
//                    text(text)
//                    textColor(Color.WHITE)
//                    backgroundColor(context.resources.getColor(R.color.colorSecondary))
//                    arrowColor(context.resources.getColor(R.color.colorSecondary))
//                    gravity(gravity)
//                    animated(true)
//                    transparentOverlay(true)
//                    showArrow(showArrow)
//                    if (modal) {
//                        modal(true)
//                        dismissOnOutsideTouch(false)
//                        dismissOnInsideTouch(true)
//                    }
//                    if (onDismissCallback != null) {
//                        onDismissListener { onDismissCallback() }
//                    }
//                }.build().show()
//            }
//        }


        /**
        Invoke lambda via IO coroutine dispatcher
         */
        fun dispatch(lambda: () -> Unit): Job {
            return CoroutineScope(Job() + Dispatchers.IO).launch {
                Log.v(TAG, "Dispatching on thread ${Thread.currentThread().name}")
                lambda.invoke()
            }
        }

    }
}