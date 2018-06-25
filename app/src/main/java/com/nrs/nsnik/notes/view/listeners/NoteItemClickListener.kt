package com.nrs.nsnik.notes.view.listeners

interface NoteItemClickListener {
    fun onClick(position: Int, itemViewType: Int)

    fun onLongClick(position: Int, itemViewType: Int)
}
