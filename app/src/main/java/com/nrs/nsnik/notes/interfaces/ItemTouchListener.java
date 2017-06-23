package com.nrs.nsnik.notes.interfaces;


import android.support.v7.widget.RecyclerView;

public interface ItemTouchListener {
    void onItemMove(int fromPosition, int toPosition, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);
    void onItemDismiss(int position);
}
