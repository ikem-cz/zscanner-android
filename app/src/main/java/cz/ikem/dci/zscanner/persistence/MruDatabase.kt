package cz.ikem.dci.zscanner.persistence

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Mru::class], version = 5, exportSchema = false)
abstract class MruDatabase : RoomDatabase() {

    abstract fun mruDao(): MruDao

    companion object {

        @Volatile
        private var mInstance: MruDatabase? = null

        fun getDatabase(context: Context, scope: Lazy<CoroutineScope>): MruDatabase {
            synchronized(this) {
                if (MruDatabase.mInstance == null) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            MruDatabase::class.java,
                            "mru"
                    )
                            .fallbackToDestructiveMigration()
                            .addCallback(MruDatabase.MruDatabaseCallback(scope))
                            .build()

                    mInstance = instance
                }
                return mInstance!!
            }
        }

    }

    private class MruDatabaseCallback(
            private val scope: Lazy<CoroutineScope>
    ) : RoomDatabase.Callback() {

        private val TAG = MruDatabaseCallback::class.java.simpleName

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            MruDatabase.mInstance?.let { database ->
                scope.value.launch {
                    populateDatabase(database.mruDao())
                }
            }
        }

        fun populateDatabase(mruDao: MruDao) {
            val initialData = listOf(
                    Mru(
                            0,
                            System.currentTimeMillis(),
                            "1183637",
                            "PACIENT Test",
                            "010101/222"
                    )
            )
            mruDao.initializeIfEmpty(initialData)
            Log.d(TAG, "MruDatabase intialized.")
        }

    }

}