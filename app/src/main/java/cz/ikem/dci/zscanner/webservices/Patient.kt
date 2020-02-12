package cz.ikem.dci.zscanner.webservices

data class Patient(
        val externalId: String, // rodne dislo
        val name: String,
        val internalId: String // cislo karty
) {
    fun getDisplay(): String {
        return "${externalId} ${name}"
    }
}