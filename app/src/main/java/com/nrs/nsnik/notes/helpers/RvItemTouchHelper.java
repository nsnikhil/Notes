package com.nrs.nsnik.notes.helpers;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.nrs.nsnik.notes.interfaces.ItemTouchListener;

import java.util.ArrayList;
import java.util.List;

public class RvItemTouchHelper extends ItemTouchHelper.Callback {

    /*
    TODO ITEM TOUCH HELPER CALL BACK DOCUMENTATION
     */

    private ItemTouchListener mListener;
    private static final String TAG = RvItemTouchHelper.class.getSimpleName();
    private static final int NOTES = 0, FOLDER = 1, HEADER = 2;
    private int mDragFromPosition = -1, mDragToPosition = -1;
    private List<Integer> mDragFromList, mDragToList;

    public RvItemTouchHelper(ItemTouchListener listener) {
        mListener = listener;
        mDragFromList = new ArrayList<>();
        mDragToList = new ArrayList<>();
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == NOTES || viewHolder.getItemViewType() == FOLDER) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
        return 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mDragFromList.add(viewHolder.getAdapterPosition());
        mDragToList.add(target.getAdapterPosition());
        mListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition(), viewHolder, target);
        return true;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (mDragFromList.size() > 0) {
            mDragFromPosition = getMax();
        }
        if (mDragToList.size() > 0) {
            mDragToPosition = getMin();
        }
        if (mDragFromPosition != -1 && mDragToPosition != -1) {
            mListener.onItemMoved(mDragFromPosition, mDragToPosition, recyclerView, viewHolder);
        }
        mDragFromList.clear();
        mDragToList.clear();
        mDragFromPosition = -1;
        mDragToPosition = -1;
    }

    private int getMax() {
        int max = mDragFromList.get(0);
        for (int i = 1; i < mDragFromList.size(); i++) {
            if (max < mDragFromList.get(i)) {
                max = mDragFromList.get(i);
            }
        }
        return max;
    }

    private int getMin() {
        int min = mDragToList.get(0);
        for (int i = 1; i < mDragToList.size(); i++) {
            if (min > mDragToList.get(i)) {
                min = mDragToList.get(i);
            }
        }
        return min;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mListener.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
