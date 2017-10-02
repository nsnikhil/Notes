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


import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.List;

@AutoValue
public abstract class NoteObject implements Parcelable, Serializable {


    public static Builder builder() {
        return new AutoValue_NoteObject.Builder();
    }

    public abstract String title();

    public abstract String noteContent();

    public abstract String folderName();

    public abstract String color();

    public abstract String time();

    public abstract List<String> imageList();

    public abstract List<String> audioList();

    public abstract List<CheckListObject> checkList();

    public abstract int isPinned();

    public abstract int isLocked();

    public abstract int hasReminder();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder title(String value);

        public abstract Builder noteContent(String value);

        public abstract Builder folderName(String value);

        public abstract Builder color(String value);

        public abstract Builder time(String value);

        public abstract Builder imageList(List<String> value);

        public abstract Builder audioList(List<String> value);

        public abstract Builder checkList(List<CheckListObject> value);

        public abstract Builder isPinned(int value);

        public abstract Builder isLocked(int value);

        public abstract Builder hasReminder(int value);

        public abstract NoteObject build();
    }
}
