package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "mru")
data class Mru(
        @PrimaryKey(autoGenerate = true)
        @SerializedName("id")
        @ColumnInfo(name = "id")
        var id: Int,

        @SerializedName("timestamp")
        @ColumnInfo(name = "timestamp")
        var timestamp: Long,

        @SerializedName("internalId")
        @ColumnInfo(name = "internalId")
        var internalId: String?,

        @SerializedName("nameOrEntered")
        @ColumnInfo(name = "nameOrEntered")
        var name: String,

        @SerializedName("externalId")
        @ColumnInfo(name = "externalId")
        var externalId: String?
)