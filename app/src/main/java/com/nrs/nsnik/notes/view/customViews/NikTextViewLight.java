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
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.nrs.nsnik.notes.util.TypeFaceCache;


public class NikTextViewLight extends android.support.v7.widget.AppCompatTextView {

    public NikTextViewLight(@NonNull Context context) {
        super(context);
        applyCustomFont(context);
    }

    public NikTextViewLight(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public NikTextViewLight(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyCustomFont(context);
    }


    private void applyCustomFont(@NonNull Context context) {
        Typeface customFont = TypeFaceCache.getTypeface("roboto-mono-regular.ttf", context);
        setTypeface(customFont);
    }

    @Override
    public void setLetterSpacing(float letterSpacing) {
        super.setLetterSpacing(0.04f);
    }

}
