package com.nrs.nsnik.notes.interfaces;


import android.support.v7.widget.RecyclerView;

public interface ItemTouchListener {

    /*
    called any time item is moving

    @param fromPosition         the starting position of the dragging operation
    @param toPosition           the end position of the dragging operation
    @param viewHolder           the viewholder of the starting item that was dragged
    @param target                the viewholder of the item ao toPostition
     */
    void onItemMove(int fromPosition, int toPosition, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

    /*
    called once the item have finished moving

     @param fromPosition         the starting position of the dragging operation
     @param toPosition           the end position of the dragging operation
     @param recyclerView         the recycler View on which the operation happened
     @param viewHolder           the viewholder of the starting item that was dragged
    */
    void onItemMoved(int fromPosition, int toPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

    /*
    @param position     the position at which item was swiped/removed
     */
    void onItemDismiss(int position);
}
