package cz.ikem.dci.zscanner.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope

@Database(entities = [DocumentSubType::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DocumentSubTypeDatabase : RoomDatabase() {

    abstract fun documentSubTypeDao(): DocumentSubTypeDao

    companion object {

        @Volatile
        private var mInstance: DocumentSubTypeDatabase? = null

        fun getDatabase(context: Context, scope: Lazy<CoroutineScope>): DocumentSubTypeDatabase {
            synchronized(this) {
                if (mInstance == null) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            DocumentSubTypeDatabase::class.java,
                            "document_sub_type"
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