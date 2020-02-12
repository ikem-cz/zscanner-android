package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mru")
data class Mru(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        var id: Int,
        @ColumnInfo(name = "timestamp")
        var timestamp: Long,
        @ColumnInfo(name = "internalId")
        var internalId: String?,
        @ColumnInfo(name = "nameOrEntered")
        var name: String,
        @ColumnInfo(name = "externalId")
        var externalId: String?
)