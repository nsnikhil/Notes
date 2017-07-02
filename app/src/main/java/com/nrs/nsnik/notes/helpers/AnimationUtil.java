package com.nrs.nsnik.notes.helpers;


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
