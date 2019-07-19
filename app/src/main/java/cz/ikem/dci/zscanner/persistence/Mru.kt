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
        @ColumnInfo(name = "zid")
        var zid: String?,
        @ColumnInfo(name = "nameOrEntered")
        var name: String,
        @ColumnInfo(name = "bid")
        var bid: String?
)