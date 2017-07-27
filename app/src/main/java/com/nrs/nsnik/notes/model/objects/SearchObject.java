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


public class SearchObject {

    private String mName;
    private boolean mIsFolder;

    SearchObject(SearchObjectBuilder searchObjectBuilder) {
        mName = searchObjectBuilder.mName;
        mIsFolder = searchObjectBuilder.mIsFolder;
    }

    public String getmName() {
        return mName;
    }

    public boolean ismIsFolder() {
        return mIsFolder;
    }

    public static class SearchObjectBuilder {

        private String mName;
        private boolean mIsFolder;

        public SearchObjectBuilder setName(String name) {
            mName = name;
            return this;
        }

        public SearchObjectBuilder setIsFolder(boolean isFolder) {
            mIsFolder = isFolder;
            return this;
        }

        public SearchObject build() {
            return new SearchObject(this);
        }
    }
}