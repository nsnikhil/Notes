package com.nrs.nsnik.notes.view.listeners

import android.view.View

interface NoteItemClickListener {
    fun onClick(position: Int, itemViewType: Int)

    fun onLongClick(position: Int, itemViewType: Int, view: View)
}
