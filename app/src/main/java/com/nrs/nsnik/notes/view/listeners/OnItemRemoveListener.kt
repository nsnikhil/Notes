package com.nrs.nsnik.notes.view.listeners

interface OnItemRemoveListener {
    fun onItemRemoved(position: Int, adapterType: AdapterType)
}

public enum class AdapterType {
    IMAGE_ADAPTER, AUDIO_ADAPTER, CHECKLIST_ADAPTER
}