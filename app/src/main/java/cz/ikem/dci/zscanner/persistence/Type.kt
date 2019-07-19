package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.ikem.dci.zscanner.screen_message.CreateMessageMode
import cz.ikem.dci.zscanner.screen_message.ModeDispatcher

@Entity(tableName = "document_type")
data class Type(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Int,
        @ColumnInfo(name = "type")
        val type: String,
        @ColumnInfo(name = "mode")
        val mode: CreateMessageMode,
        @ColumnInfo(name = "display")
        val display: String
)