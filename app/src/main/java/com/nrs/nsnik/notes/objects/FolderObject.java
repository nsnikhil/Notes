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

public class FolderObject implements Serializable, Parcelable {

    public static final Creator<FolderObject> CREATOR = new Creator<FolderObject>() {
        @Override
        public FolderObject createFromParcel(Parcel in) {
            return new FolderObject(in);
        }

        @Override
        public FolderObject[] newArray(int size) {
            return new FolderObject[size];
        }
    };
    private String mFolderName, mFolderColor;

    FolderObject(FolderObjectBuilder folderObjectBuilder) {
        mFolderName = folderObjectBuilder.mFolderName;
        mFolderColor = folderObjectBuilder.mFolderColor;
    }

    private FolderObject(Parcel in) {
        mFolderName = in.readString();
        mFolderColor = in.readString();
    }

    public String getmFolderName() {
        return mFolderName;
    }

    public String getmFolderColor() {
        return mFolderColor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mFolderName);
        parcel.writeString(mFolderColor);
    }

    public static class FolderObjectBuilder {

        private String mFolderName, mFolderColor;

        public FolderObjectBuilder setFolderName(String folderName) {
            this.mFolderName = folderName;
            return this;
        }

        public FolderObjectBuilder setFolderColor(String folderColor) {
            this.mFolderColor = folderColor;
            return this;
        }

        public FolderObject build() {
            return new FolderObject(this);
        }
    }

}
