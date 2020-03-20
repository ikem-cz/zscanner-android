package cz.ikem.dci.zscanner.webservices

import com.google.gson.annotations.SerializedName

data class Patient(
        @SerializedName("externalId")
        val externalId: String, // rodne dislo
        @SerializedName("name")
        val name: String,
        @SerializedName("internalId")
        val internalId: String // cislo karty
) {
    fun getDisplay(): String {
        return "${externalId} ${name}"
    }
}