package cz.ikem.dci.zscanner.persistence

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import cz.ikem.dci.zscanner.screen_message.CreateMessageMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Type::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TypeDatabase : RoomDatabase() {

    abstract fun documentTypeDao(): TypeDao

    companion object {

        @Volatile
        private var mInstance: TypeDatabase? = null

        fun getDatabase(context: Context, scope: Lazy<CoroutineScope>): TypeDatabase {
            synchronized(this) {
                if (mInstance == null) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                            context.applicationContext,
                            TypeDatabase::class.java,
                            "document_type"
                    )
                            .fallbackToDestructiveMigration()
                            .addCallback(DocumentTypeDatabaseCallback(scope))
                            .build()

                    mInstance = instance
                }
                return mInstance!!
            }
        }
    }

    private class DocumentTypeDatabaseCallback(
            private val scope: Lazy<CoroutineScope>
    ) : RoomDatabase.Callback() {

        private val TAG = DocumentTypeDatabaseCallback::class.java.simpleName

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            mInstance?.let { database ->
                scope.value.launch {
                    populateDatabase(database.documentTypeDao())
                }
            }
        }

        fun populateDatabase(dtDao: TypeDao) {
            val initialData = listOf(
                    Type(0,"amb_zpr", CreateMessageMode.DOCUMENT, "Ambulantní dokument"),
                    Type(0,"HOSP_ADM", CreateMessageMode.DOCUMENT, "Přijímací zprava"),
                    Type(0,"HOSP_DIS", CreateMessageMode.DOCUMENT, "Propouštěcí zprava"),
                    Type(0,"EXT", CreateMessageMode.EXAM, "Externí vyšetření"),
                    Type(0,"ECG", CreateMessageMode.EXAM, "EKG záznam")
            )

            dtDao.updateTypesTransactionIfEmpty(initialData)
            Log.d(TAG, "TypeDatabase intialized.")
        }

    }


}
