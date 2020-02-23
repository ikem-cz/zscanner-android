package cz.ikem.dci.zscanner

import cz.ikem.dci.zscanner.persistence.Mru


interface OnCreateMessageViewsInteractionListener {
    fun onScanPatientIdButtonPress()
    fun onCapturePagePhotoButtonPress()
}

interface OnStartDragListener {
    fun onStartDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder)
}

interface KeyboardCallback {
    fun hideKeyboard()
}

interface MruSelectionCallback {
    fun onMruSelected(mru: Mru)
}