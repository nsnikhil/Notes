package com.nrs.nsnik.notes.customViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.nrs.nsnik.notes.helpers.TypeFaceCache;


public class NikTextView extends android.support.v7.widget.AppCompatTextView {

    public NikTextView(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public NikTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public NikTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont(context);
    }


    private void applyCustomFont(Context context) {
        Typeface customFont = TypeFaceCache.getTypeface("roboto-mono-medium.ttf", context);
        setTypeface(customFont);
    }

    @Override
    public void setLetterSpacing(float letterSpacing) {
        super.setLetterSpacing(0.04f);
    }
}
