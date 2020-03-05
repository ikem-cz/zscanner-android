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

@Database(entities = [DocumentType::class], version = 12, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DocumentTypeDatabase : RoomDatabase() {

    abstract fun documentTypeDao(): DocumentTypeDao

    companion object {

        @Volatile
        private var mInstance: DocumentTypeDatabase? = null

        fun getDatabase(context: Context, scope: Lazy<CoroutineScope>): DocumentTypeDatabase {
            synchronized(this) {
                if (mInstance == null) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            DocumentTypeDatabase::class.java,
                            "document_type"
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
