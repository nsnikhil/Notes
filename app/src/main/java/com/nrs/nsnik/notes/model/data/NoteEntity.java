/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.model.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    private int mUid;
    private String mTitle;
    private String mFileName;
    private String mFolderName;
    private int mIsPinned;
    private int mIsLocked;
    private String mDateModified;
    private String mColor;

    NoteEntity(int uid, String title, String fileName, String folderName, int isPinned, int isLocked, String dateModified, String color) {
        mUid = uid;
        mTitle = title;
        mFileName = fileName;
        mFolderName = folderName;
        mIsPinned = isPinned;
        mIsLocked = isLocked;
        mDateModified = dateModified;
        mColor = color;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public String getFolderName() {
        return this.mFolderName;
    }

    public void setFolderName(String folderName) {
        this.mFolderName = folderName;
    }

    public int getIsPinned() {
        return this.mIsPinned;
    }

    public void setIsPinned(int isPinned) {
        this.mIsPinned = isPinned;
    }

    public int getIsLocked() {
        return this.mIsLocked;
    }

    public void setIsLocked(int isLocked) {
        this.mIsLocked = isLocked;
    }

    public String getDateModified() {
        return this.mDateModified;
    }

    public void setDateModified(String dateModified) {
        this.mDateModified = dateModified;
    }

    public String getColor() {
        return this.mColor;
    }

    public void setColor(String color) {
        this.mColor = color;
    }
}
