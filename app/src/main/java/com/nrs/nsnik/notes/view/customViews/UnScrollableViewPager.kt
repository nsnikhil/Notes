/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * custom implementation of view pager
 * with swipe to scroll disabled
 */

class UnScrollableViewPager : ViewPager {

    /**
     * @param context the context object
     */
    constructor(context: Context) : super(context)

    /**
     * @param context the context object
     * @param attrs   group of attributes associated with the view on xml
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     * @return false for all touch screen motion events
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    /**
     * @return false i.e. no implementation to handle touch screen motion events.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}
