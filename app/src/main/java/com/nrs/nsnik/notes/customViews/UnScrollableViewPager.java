package com.nrs.nsnik.notes.customViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/*
custom implementation of view pager
with swipe to scroll disabled
 */

public class UnScrollableViewPager extends ViewPager {

    /*
    @param context  the context object
     */
    public UnScrollableViewPager(Context context) {
        super(context);
    }

    /*
    @param context  the context object
    @param attr     group of attributes associated with the view on xml
     */
    public UnScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
    return false for all touch screen motion events
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    /*
    returns false i.e. no implementation to handle touch screen motion events.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
