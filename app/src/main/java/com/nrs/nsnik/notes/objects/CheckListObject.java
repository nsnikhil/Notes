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

public class CheckListObject implements Serializable, Parcelable {

    private static final Creator<CheckListObject> CREATOR = new Creator<CheckListObject>() {
        @Override
        public CheckListObject createFromParcel(Parcel in) {
            return new CheckListObject(in);
        }

        @Override
        public CheckListObject[] newArray(int size) {
            return new CheckListObject[size];
        }
    };
    private String mText;
    private boolean mDone;

    public CheckListObject(String text, boolean done) {
        mText = text;
        mDone = done;
    }

    private CheckListObject(Parcel in) {
        mText = in.readString();
        mDone = in.readByte() != 0;
    }


    public static Creator<CheckListObject> getCREATOR() {
        return CREATOR;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public boolean ismDone() {
        return mDone;
    }

    public void setmDone(boolean mDone) {
        this.mDone = mDone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mText);
        parcel.writeByte((byte) (mDone ? 1 : 0));
    }
}
