package com.nrs.nsnik.notes.objects;


public class SearchObject {

    private String mName;
    private boolean mIsFolder;


    /*
    TODO REPLACE WITH AUTO VALUE
     */

    public SearchObject(String name, boolean isFolder) {
        mName = name;
        mIsFolder = isFolder;
    }

    public String getmName() {
        return mName;
    }

    public boolean ismIsFolder() {
        return mIsFolder;
    }
}
