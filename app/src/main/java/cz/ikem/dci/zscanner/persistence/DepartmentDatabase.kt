package cz.ikem.dci.zscanner.persistence

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Department::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DepartmentDatabase : RoomDatabase() {

    abstract fun departmentDao(): DepartmentDao

    companion object {

        @Volatile
        private var mInstance: DepartmentDatabase? = null

        fun getDatabase(context: Context): DepartmentDatabase {
            synchronized(this) {
                if (mInstance == null) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            DepartmentDatabase::class.java,
                            "department")
                            .fallbackToDestructiveMigration()
                            .build()

                    mInstance = instance
                }
                return mInstance!!
            }
        }
    }
}