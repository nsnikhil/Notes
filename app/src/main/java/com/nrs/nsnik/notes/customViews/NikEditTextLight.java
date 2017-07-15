package com.nrs.nsnik.notes.customViews;

import android.content.Context;
import android.util.AttributeSet;

import com.nrs.nsnik.notes.helpers.TypeFaceCache;

public class NikEditTextLight extends android.support.v7.widget.AppCompatEditText {

    public NikEditTextLight(Context context) {
        super(context);
        setTypeface(TypeFaceCache.getTypeface("roboto-mono-regular.ttf", context));
    }

    public NikEditTextLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(TypeFaceCache.getTypeface("roboto-mono-regular.ttf", context));
    }

    public NikEditTextLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(TypeFaceCache.getTypeface("roboto-mono-regular.ttf", context));
    }

    @Override
    public void setLetterSpacing(float letterSpacing) {
        super.setLetterSpacing(0.04f);
    }

}
