package com.nrs.nsnik.notes.util.events;

public class FolderClickEvent {

    private final String mFolderName;

    public FolderClickEvent(String folderName) {
        mFolderName = folderName;
    }

    public String getFolderName() {
        return mFolderName;
    }
}
