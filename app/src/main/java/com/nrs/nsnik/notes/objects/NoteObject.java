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
    private String mTitle, mNoteContent, mFolderName;
    private List<String> images, audioLocations;
    private List<CheckListObject> mCheckList;

    /*
    TODO REPLACE WITH AUTO VALUE
     */
    private int reminder;

    public NoteObject(String title, String note, ArrayList<String> images, List<String> audioLocations, List<CheckListObject> checkList, int reminder, String folderName) {
        this.mNoteContent = note;
        this.mTitle = title;
        this.images = images;
        this.audioLocations = audioLocations;
        this.mCheckList = checkList;
        this.reminder = reminder;
        this.mFolderName = folderName;
    }

    private NoteObject(Parcel in) {
        mTitle = in.readString();
        mNoteContent = in.readString();
        audioLocations = in.createStringArrayList();
        mCheckList = new ArrayList<>();
        in.readList(mCheckList, List.class.getClassLoader());
        mFolderName = in.readString();
        images = in.createStringArrayList();
        reminder = in.readInt();
    }

    public String getTitle() {
        return mTitle;
    }

    public String getNote() {
        return mNoteContent;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getAudioLocations() {
        return audioLocations;
    }

    public List<CheckListObject> getmCheckList() {
        return mCheckList;
    }

    public void setmCheckList(List<CheckListObject> mCheckList) {
        this.mCheckList = mCheckList;
    }

    public int getReminder() {
        return reminder;
    }

    public String getFolderName() {
        return mFolderName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mNoteContent);
        parcel.writeStringList(audioLocations);
        parcel.writeList(mCheckList);
        parcel.writeString(mFolderName);
        parcel.writeStringList(images);
        parcel.writeInt(reminder);
    }
}
