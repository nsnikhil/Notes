/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util;

import android.support.v7.util.DiffUtil;

import com.nrs.nsnik.notes.model.objects.FolderObject;

import java.util.List;


public class FolderDiffCallback extends DiffUtil.Callback {

    /*
    TODO DIFFERENCE UTILITY CLASS FOR FOLDER LISTtodo
     */

    private List<FolderObject> mOldList, mNewList;

    public FolderDiffCallback(List<FolderObject> oldList, List<FolderObject> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).getmFolderName().equalsIgnoreCase(mNewList.get(newItemPosition).getmFolderName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }
}
