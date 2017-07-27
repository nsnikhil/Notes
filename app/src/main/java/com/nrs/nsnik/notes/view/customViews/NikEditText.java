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
import android.util.AttributeSet;

import com.nrs.nsnik.notes.util.TypeFaceCache;

public class NikEditText extends android.support.v7.widget.AppCompatEditText {

    public NikEditText(Context context) {
        super(context);
        setTypeface(TypeFaceCache.getTypeface("roboto-mono-medium.ttf", context));
    }

    public NikEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(TypeFaceCache.getTypeface("roboto-mono-medium.ttf", context));
    }

    public NikEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(TypeFaceCache.getTypeface("roboto-mono-medium.ttf", context));
    }

    @Override
    public void setLetterSpacing(float letterSpacing) {
        super.setLetterSpacing(0.04f);
    }
}