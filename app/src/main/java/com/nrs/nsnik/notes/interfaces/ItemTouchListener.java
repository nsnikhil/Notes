package com.nrs.nsnik.notes.interfaces;


public interface ItemTouchListener {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
