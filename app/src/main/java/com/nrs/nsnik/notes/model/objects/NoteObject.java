/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.model.objects;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NoteObject implements Serializable, Parcelable {

    public static final Creator<NoteObject> CREATOR = new Creator<NoteObject>() {
        @NonNull
        @Override
        public NoteObject createFromParcel(@NonNull Parcel in) {
            return new NoteObject(in);
        }

        @NonNull
        @Contract(pure = true)
        @Override
        public NoteObject[] newArray(int size) {
            return new NoteObject[size];
        }
    };
    private final String mTitle, mNoteContent, mFolderName, mColor, mTime;
    private final List<String> mImagesList, mAudioList;
    private final List<CheckListObject> mCheckList;
    private final int mIsPinned, mIsLocked, mReminder;

    /*
    TODO REPLACE WITH AUTO VALUE
     */

    NoteObject(@NonNull NoteObjectBuilder noteObjectBuilder) {
        mNoteContent = noteObjectBuilder.mNoteContent;
        mTitle = noteObjectBuilder.mTitle;
        mImagesList = noteObjectBuilder.mImagesList;
        mAudioList = noteObjectBuilder.mAudioList;
        mCheckList = noteObjectBuilder.mCheckList;
        mReminder = noteObjectBuilder.mReminder;
        mFolderName = noteObjectBuilder.mFolderName;
        mColor = noteObjectBuilder.mColor;
        mTime = noteObjectBuilder.mTime;
        mIsPinned = noteObjectBuilder.mIsPinned;
        mIsLocked = noteObjectBuilder.mIsLocked;
    }

    private NoteObject(@NonNull Parcel in) {
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
    public void writeToParcel(@NonNull Parcel parcel, int i) {
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

    public static class NoteObjectBuilder {

        private String mTitle, mNoteContent, mFolderName, mColor, mTime;
        private List<String> mImagesList, mAudioList;
        private List<CheckListObject> mCheckList;
        private int mIsPinned, mIsLocked, mReminder;

        @NonNull
        public NoteObjectBuilder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setNoteContent(String noteContent) {
            this.mNoteContent = noteContent;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setFolderName(String folderName) {
            this.mFolderName = folderName;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setColor(String color) {
            this.mColor = color;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setTime(String time) {
            this.mTime = time;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setImageList(List<String> imageList) {
            this.mImagesList = imageList;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setAudioList(List<String> audioList) {
            this.mAudioList = audioList;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setCheckList(List<CheckListObject> checkList) {
            this.mCheckList = checkList;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setPinned(int pinned) {
            this.mIsPinned = pinned;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setLocked(int locked) {
            this.mIsLocked = locked;
            return this;
        }

        @NonNull
        public NoteObjectBuilder setHasReminder(int reminder) {
            this.mReminder = reminder;
            return this;
        }

        @NonNull
        public NoteObject build() {
            return new NoteObject(this);
        }
    }
}
