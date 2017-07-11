package com.nrs.nsnik.notes.objects;


public class CheckListObject {

    private String mText;
    private boolean mDone;

    CheckListObject(String text, boolean done) {
        mText = text;
        mDone = done;
    }

    public String getmText() {
        return mText;
    }

    public boolean ismDone() {
        return mDone;
    }
}
