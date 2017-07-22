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


import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;


public class AnimationUtil {

    /*
    TODO ANIMATION FOR RECYCLER VIEW
     */

    public static void animateItems(RecyclerView.ViewHolder holder) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(holder.itemView, "translationY", 0, 100);
        animator.setDuration(1000);
        animator.start();
    }

}
