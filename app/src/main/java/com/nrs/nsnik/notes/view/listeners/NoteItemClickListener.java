package com.nrs.nsnik.notes.view.listeners;

public interface NoteItemClickListener {
    void onClick(int position, int itemViewType);

    void onLongClick(int position, int itemViewType);
}
