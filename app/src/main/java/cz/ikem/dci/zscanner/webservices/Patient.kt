package cz.ikem.dci.zscanner.webservices

data class Patient(
        val bid: String, // rodne dislo
        val name: String,
        val zid: String // cislo karty
) {
    fun getDisplay(): String {
        return "${bid} ${name}"
    }
}