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
public class FolderEntity {

    @PrimaryKey(autoGenerate = true)
    private int mUid;
    private String mFolderName;
    private String mFolderId;
    private String mParentFolderName;
    private int mIsPinned;
    private int mIsLocked;
    private String mColor;

    public int getUid() {
        return mUid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public String getFolderName() {
        return this.mFolderName;
    }

    public void setFolderName(String folderName) {
        this.mFolderName = folderName;
    }

    public String getFolderId() {
        return this.mFolderId;
    }

    public void setFolderId(String folderId) {
        this.mFolderId = folderId;
    }

    public String getParentFolderName() {
        return this.mParentFolderName;
    }

    public void setParentFolderName(String parentFolderName) {
        this.mParentFolderName = parentFolderName;
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

    public String getColor() {
        return this.mColor;
    }

    public void setColor(String color) {
        this.mColor = color;
    }
}
