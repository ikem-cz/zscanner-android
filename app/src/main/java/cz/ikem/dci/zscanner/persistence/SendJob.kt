package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import cz.ikem.dci.zscanner.screen_message.PageActionsQueue
import org.json.JSONArray

@Entity(tableName = "send_jobs")
@TypeConverters(Converters::class)
data class SendJob(
        @PrimaryKey
        @SerializedName("instance_id")
        @ColumnInfo(name = "instance_id")
        var correlationId: String,

        @SerializedName("timestamp")
        @ColumnInfo(name = "timestamp")
        var timestamp: Long,

        @SerializedName("patient_id")
        @ColumnInfo(name = "patient_id")
        var patientId: String,

        @SerializedName("document_type")
        @ColumnInfo(name = "document_type")
        var docType: String,

        @SerializedName("document_sub_type")
        @ColumnInfo(name = "document_sub_type")
        var docSubType: String?,

        @SerializedName("document_note")
        @ColumnInfo(name = "document_note")
        var docName: String,

        @SerializedName("document_datetime")
        @ColumnInfo(name = "document_datetime")
        var docDateTime: String,

        @SerializedName("pages")
        @ColumnInfo(name = "pages")
        var pageFiles: List<PageActionsQueue.Page>,

        @SerializedName("internal_num_tasks")
        @ColumnInfo(name = "internal_num_tasks")
        var intSumTasks: Int,

        @SerializedName("internal_done_tasks")
        @ColumnInfo(name = "internal_done_tasks")
        var intDoneTasks: List<String>,

        @SerializedName("internal_document_descr")
        @ColumnInfo(name = "internal_document_descr")
        var intDocDescr: String,

        @SerializedName("internal_patient_display")
        @ColumnInfo(name = "internal_patient_display")
        var intPatDisplay: String
)

