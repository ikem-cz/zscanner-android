package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "document_type")
data class DocumentType(
        @PrimaryKey
        @SerializedName("type_id_plus_department_id")
        @ColumnInfo(name = "type_id_plus_department_id")
        val docTypeIdPlusDeptId: String,

        @SerializedName("id")
        @ColumnInfo(name = "id")
        val id: String,

        @SerializedName("display")
        @ColumnInfo(name = "display")
        val display: String,

        @SerializedName("sub-type")
        @ColumnInfo(name = "sub-type")
        val subtype: String? = null,

        @SerializedName("department_id")
        @ColumnInfo(name = "department_id")
        val departmentId: String
)

@Entity(tableName = "document_sub_type")
data class DocumentSubType(
        @PrimaryKey
        @SerializedName("id")
        @ColumnInfo(name = "id")
        val id: String,
        @SerializedName("display")
        @ColumnInfo(name = "display")
        val display: String
)