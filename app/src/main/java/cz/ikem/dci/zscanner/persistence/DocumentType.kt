package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_type")
data class DocumentType(
        @PrimaryKey
        @ColumnInfo(name = "type_id_plus_department_id")
        val docTypeIdPlusDeptId: String,
        @ColumnInfo(name = "id")
        val id: String,
        @ColumnInfo(name = "display")
        val display: String,
        @ColumnInfo(name = "sub-types")
        val subtype: String? = null,
        @ColumnInfo(name = "department_id")
        val departmentId: String
)

@Entity(tableName = "document_sub_type")
data class DocumentSubType(
        @PrimaryKey
        @ColumnInfo(name = "id")
        val id: String,
        @ColumnInfo(name = "display")
        val display: String
)