package cz.ikem.dci.zscanner.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.ikem.dci.zscanner.screen_message.CreateMessageMode

@Entity(tableName = "send_jobs")
data class SendJob(
        @PrimaryKey
        @ColumnInfo(name = "instance_id")
        var correlationId: String,
        @ColumnInfo(name = "timestamp")
        var timestamp: Long,
        @ColumnInfo(name = "patient_id")
        var patientId: String,
        @ColumnInfo(name = "document_mode")
        var docMode: CreateMessageMode,
        @ColumnInfo(name = "document_type")
        var docType: String,
        @ColumnInfo(name = "document_name")
        var docName: String,
        @ColumnInfo(name = "document_datetime")
        var docDateTime: String,
        @ColumnInfo(name = "pages")
        var pageFiles: List<String>,
        @ColumnInfo(name = "internal_num_tasks")
        var intSumTasks: Int,
        @ColumnInfo(name = "internal_done_tasks")
        var intDoneTasks: List<String>,
        @ColumnInfo(name = "internal_document_descr")
        var intDocDescr: String,
        @ColumnInfo(name = "internal_patient_display")
        var intPatDisplay: String
)

