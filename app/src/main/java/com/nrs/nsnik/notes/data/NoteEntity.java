/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import com.nrs.nsnik.notes.model.CheckListObject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

@Entity
public class NoteEntity implements Serializable, Parcelable {

    public static final Creator<NoteEntity> CREATOR = new Creator<NoteEntity>() {
        @Override
        public NoteEntity createFromParcel(Parcel in) {
            return new NoteEntity(in);
        }

        @Override
        public NoteEntity[] newArray(int size) {
            return new NoteEntity[size];
        }
    };
    @PrimaryKey(autoGenerate = true)
    private int mUid;
    private String mTitle;
    @Ignore
    private String mNoteContent;
    @Nullable
    private String mFileName;
    private String mFolderName;
    private int mIsPinned;
    private int mIsLocked;
    @TypeConverters(DateConverter.class)
    private Date mDateModified;
    private String mColor;
    @Ignore
    private List<String> mImageList;
    @Ignore
    private List<String> mAudioList;
    @Ignore
    private List<CheckListObject> mCheckList;
    @Ignore
    private int hasReminder;

    public NoteEntity() {

    }

    protected NoteEntity(Parcel in) {
        mUid = in.readInt();
        mTitle = in.readString();
        mNoteContent = in.readString();
        mFileName = in.readString();
        mFolderName = in.readString();
        mIsPinned = in.readInt();
        mIsLocked = in.readInt();
        mColor = in.readString();
        mImageList = in.createStringArrayList();
        mAudioList = in.createStringArrayList();
        mCheckList = in.createTypedArrayList(CheckListObject.getCreator());
        hasReminder = in.readInt();
    }

    public int getUid() {
        return mUid;
    }

    public void setUid(int mUid) {
        this.mUid = mUid;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getNoteContent() {
        return mNoteContent;
    }

    public void setNoteContent(String mNoteContent) {
        this.mNoteContent = mNoteContent;
    }

    @Nullable
    public String getFileName() {
        return mFileName;
    }

    public void setFileName(@Nullable String mFileName) {
        this.mFileName = mFileName;
    }

    public String getFolderName() {
        return mFolderName;
    }

    public void setFolderName(String mFolderName) {
        this.mFolderName = mFolderName;
    }

    public int getIsPinned() {
        return mIsPinned;
    }

    public void setIsPinned(int mIsPinned) {
        this.mIsPinned = mIsPinned;
    }

    public int getIsLocked() {
        return mIsLocked;
    }

    public void setIsLocked(int mIsLocked) {
        this.mIsLocked = mIsLocked;
    }

    public Date getDateModified() {
        return mDateModified;
    }

    public void setDateModified(Date mDateModified) {
        this.mDateModified = mDateModified;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String mColor) {
        this.mColor = mColor;
    }

    public List<String> getImageList() {
        return mImageList;
    }

    public void setImageList(List<String> mImageList) {
        this.mImageList = mImageList;
    }

    public List<String> getAudioList() {
        return mAudioList;
    }

    public void setAudioList(List<String> mAudioList) {
        this.mAudioList = mAudioList;
    }

    public List<CheckListObject> getCheckList() {
        return mCheckList;
    }

    public void setCheckList(List<CheckListObject> mCheckList) {
        this.mCheckList = mCheckList;
    }

    public int getHasReminder() {
        return hasReminder;
    }

    public void setHasReminder(int hasReminder) {
        this.hasReminder = hasReminder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mUid);
        parcel.writeString(mTitle);
        parcel.writeString(mNoteContent);
        parcel.writeString(mFileName);
        parcel.writeString(mFolderName);
        parcel.writeInt(mIsPinned);
        parcel.writeInt(mIsLocked);
        parcel.writeString(mColor);
        parcel.writeStringList(mImageList);
        parcel.writeStringList(mAudioList);
        parcel.writeTypedList(mCheckList);
        parcel.writeInt(hasReminder);
    }
}
