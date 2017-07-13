/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

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
