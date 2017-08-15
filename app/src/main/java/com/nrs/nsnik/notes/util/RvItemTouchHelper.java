/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.nrs.nsnik.notes.util.interfaces.ItemTouchListener;

import java.util.ArrayList;
import java.util.List;

public class RvItemTouchHelper extends ItemTouchHelper.Callback {


    private static final int NOTES = 0, FOLDER = 1, HEADER = 2;
    private final ItemTouchListener mListener;
    private final List<Integer> mDragFromList, mDragToList;
    private int mDragFromPosition = -1, mDragToPosition = -1;

    /*
    @param listener     instance of ItemTouchListener
     */
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
        /*
        if view type if note or folder
        then only allow dragging or swiping
         */
        if (viewHolder.getItemViewType() == NOTES || viewHolder.getItemViewType() == FOLDER) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
        return 0;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        /*
         as the item is dragged add each starting
         and ending position of that item in a list
         */
        mDragFromList.add(viewHolder.getAdapterPosition());
        mDragToList.add(target.getAdapterPosition());
        mListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition(), viewHolder, target);
        return true;
    }

    /*
    clearView is called once the user have complete the dragging
    or swiping operations
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        /*
        get the maximum and minimum position i.e.
        the range upto whihc the item was dragged
         and pass that range to adapter
         */
        if (mDragFromList.size() > 0) {
            mDragFromPosition = getMax();
        }
        if (mDragToList.size() > 0) {
            mDragToPosition = getMin();
        }
        if (mDragFromPosition != -1 && mDragToPosition != -1) {
            mListener.onItemMoved(mDragFromPosition, mDragToPosition, recyclerView, viewHolder);
        }
        /*
        clear the list and values after passing to adapter
         */
        mDragFromList.clear();
        mDragToList.clear();
        mDragFromPosition = -1;
        mDragToPosition = -1;
    }

    /*
    returns the maximum value from starting index
    indicates from which index should the
    id swapping start
     */
    private int getMax() {
        int max = mDragFromList.get(0);
        for (int i = 1; i < mDragFromList.size(); i++) {
            if (max < mDragFromList.get(i)) {
                max = mDragFromList.get(i);
            }
        }
        return max;
    }

    /*
    returns the minimum value from ending index
    indicates to which index should the
    id swapping end
    */
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
