package cz.ikem.dci.zscanner.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SendJob::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SendJobDatabase : RoomDatabase() {

    abstract fun sendJobDao(): SendJobDao

    companion object {

        @Volatile
        private var mInstance: SendJobDatabase? = null

        fun getDatabase(context: Context): SendJobDatabase {
            synchronized(this) {
                if (mInstance == null) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            SendJobDatabase::class.java,
                            "send_job"
                    )
                            .fallbackToDestructiveMigration()
                            .build()

                    mInstance = instance
                }
                return mInstance!!
            }
        }
    }
}