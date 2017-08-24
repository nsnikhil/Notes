/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.nrs.nsnik.notes.R;

    /*
    NO INFO AVAILABLE SINCE IT IS INCOMPLETE

    TODO COMPLETE THIS PAGE INDICATOR
     */

public class PageIndicator extends View {

    private Paint mPaint;

    public PageIndicator(Context context) {
        super(context);
    }

    public PageIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.PageIndicator);
        int count = typedArray.getInt(R.styleable.PageIndicator_piv_count, 0);
        typedArray.recycle();
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        mPaint.setColor(Color.parseColor("#FFFFFFFF"));
        canvas.drawCircle(x, y, y, mPaint);
    }
}
