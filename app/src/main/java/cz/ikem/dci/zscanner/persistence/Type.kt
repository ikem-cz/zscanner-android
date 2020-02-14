package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_type")
data class Type(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: String,
        @ColumnInfo(name = "display")
        val display: String,
        @ColumnInfo(name = "subtype")
        val subtype: String? = null
)

@Entity(tableName = "document_sub_type")
data class SubType(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: String,
        @ColumnInfo(name = "display")
        val display: String
)