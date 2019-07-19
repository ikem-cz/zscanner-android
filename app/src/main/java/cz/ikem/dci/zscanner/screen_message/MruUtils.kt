package cz.ikem.dci.zscanner.screen_message

import android.content.Context
import cz.ikem.dci.zscanner.Utils.Companion.dispatch
import cz.ikem.dci.zscanner.persistence.Mru
import cz.ikem.dci.zscanner.persistence.Repositories
import cz.ikem.dci.zscanner.webservices.Patient

class MruUtils(private val context: Context) {

    fun addMru(patient: Patient) {
        val mru = Mru(0, System.currentTimeMillis(), patient.zid, patient.name, patient.bid)

        dispatch {
            val repository = Repositories(context).mruRepository
            repository.smartInsert(mru)
        }
    }

}