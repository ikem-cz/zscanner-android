package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "department")
data class Department(
        @PrimaryKey
        @SerializedName("id")
        @ColumnInfo(name = "id")
        val id: String,
        @SerializedName("display")
        @ColumnInfo(name = "display")
        val display: String
) : Serializable