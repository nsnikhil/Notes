/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.objects;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteObject implements Serializable, Parcelable {

    public static final Creator<NoteObject> CREATOR = new Creator<NoteObject>() {
        @Override
        public NoteObject createFromParcel(Parcel in) {
            return new NoteObject(in);
        }

        @Override
        public NoteObject[] newArray(int size) {
            return new NoteObject[size];
        }
    };
    private String mTitle, mNoteContent, mFolderName, mColor, mTime;
    private List<String> mImagesList, mAudioList;
    private List<CheckListObject> mCheckList;
    private int mIsPinned, mIsLocked, mReminder;

    /*
    TODO REPLACE WITH AUTO VALUE
     */

    public NoteObject(String title, String note, List<String> images, List<String> audioLocations, List<CheckListObject> checkList, int reminder, String folderName
            , String color, String time, int isPinned, int isLocked) {
        mNoteContent = note;
        mTitle = title;
        mImagesList = images;
        mAudioList = audioLocations;
        mCheckList = checkList;
        mReminder = reminder;
        mFolderName = folderName;
        mColor = color;
        mTime = time;
        mIsPinned = isPinned;
        mIsLocked = isLocked;
    }

    private NoteObject(Parcel in) {
        mTitle = in.readString();
        mNoteContent = in.readString();
        mAudioList = in.createStringArrayList();
        mCheckList = new ArrayList<>();
        in.readList(mCheckList, CheckListObject.class.getClassLoader());
        mFolderName = in.readString();
        mImagesList = in.createStringArrayList();
        mReminder = in.readInt();
        mColor = in.readString();
        mTime = in.readString();
        mIsPinned = in.readInt();
        mIsLocked = in.readInt();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getNote() {
        return mNoteContent;
    }

    public List<String> getImages() {
        return mImagesList;
    }

    public List<String> getAudioLocations() {
        return mAudioList;
    }

    public List<CheckListObject> getmCheckList() {
        return mCheckList;
    }

    public int getReminder() {
        return mReminder;
    }

    public String getFolderName() {
        return mFolderName;
    }

    public String getmColor() {
        return mColor;
    }

    public String getmTime() {
        return mTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getmIsPinned() {
        return mIsPinned;
    }

    public int getmIsLocked() {
        return mIsLocked;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mNoteContent);
        parcel.writeStringList(mAudioList);
        parcel.writeList(mCheckList);
        parcel.writeString(mFolderName);
        parcel.writeStringList(mImagesList);
        parcel.writeInt(mReminder);
        parcel.writeString(mColor);
        parcel.writeString(mTime);
        parcel.writeInt(mIsPinned);
        parcel.writeInt(mIsLocked);
    }
}
