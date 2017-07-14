package com.nrs.nsnik.notes.helpers;


import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

public class TypeFaceCache {

    private static HashMap<String, Typeface> mTypeFaceCache = new HashMap<>();

    public static Typeface getTypeface(String fontName, Context context) {
        Typeface typeface = mTypeFaceCache.get(fontName);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName);
            } catch (Exception e) {
                return null;
            }
            mTypeFaceCache.put(fontName, typeface);
        }

        return typeface;
    }
}
