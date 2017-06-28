package com.nrs.nsnik.notes.interfaces;


import android.support.v7.widget.RecyclerView;

import java.util.List;

public interface ItemTouchListener {
    void onItemMove(int fromPosition, int toPosition, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);
    void onItemMoved(int fromPosition,int toPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
    void onItemDismiss(int position);
}
