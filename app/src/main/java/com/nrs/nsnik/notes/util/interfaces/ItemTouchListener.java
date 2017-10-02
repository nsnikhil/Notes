/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util.interfaces;


import android.support.v7.widget.RecyclerView;

public interface ItemTouchListener {

    /**
     * called any time item is moving
     *
     * @param fromPosition the starting position of the dragging operation
     * @param toPosition   the end position of the dragging operation
     * @param viewHolder   the viewholder of the starting item that was dragged
     * @param target       the viewholder of the item ao toPostition
     */
    void onItemMove(int fromPosition, int toPosition, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

    /**
     * called once the item have finished moving
     *
     * @param fromPosition the starting position of the dragging operation
     * @param toPosition   the end position of the dragging operation
     * @param recyclerView the recycler View on which the operation happened
     * @param viewHolder   the viewholder of the starting item that was dragged
     */
    void onItemMoved(int fromPosition, int toPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);

    /**
     * @param position     the position at which item was swiped/removed
     */
    void onItemDismiss(int position);
}
