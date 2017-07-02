package com.nrs.nsnik.notes.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    protected void onDraw(Canvas canvas) {
        int x = getWidth() / 2;
        int y = getHeight() / 2;
        mPaint.setColor(Color.parseColor("#FFFFFFFF"));
        canvas.drawCircle(x, y, y, mPaint);
    }
}
