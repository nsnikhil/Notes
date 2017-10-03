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

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SearchObject {

    @NonNull
    public static Builder builder() {
        return new AutoValue_SearchObject.Builder();
    }

    public abstract String name();

    public abstract boolean isFolder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setName(String value);

        public abstract Builder setIsFolder(boolean value);

        public abstract SearchObject build();
    }
}
