/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.nrs.nsnik.notes.model.data.FolderEntity;
import com.nrs.nsnik.notes.model.data.NotesDatabase;
import com.nrs.nsnik.notes.view.MyApplication;

import java.util.List;

public class FolderViewModel extends AndroidViewModel {

    private LiveData<List<FolderEntity>> mFolderList;
    private NotesDatabase mNotesDatabase;
    private String mParentFolderName;

    public FolderViewModel(Application application, String parentFolderName) {
        super(application);
        mParentFolderName = parentFolderName;
        mNotesDatabase = ((MyApplication) application).getNotesDatabase();
        mFolderList = mNotesDatabase.getFolderDao().getFolderBYParent(mParentFolderName);
    }

    public LiveData<List<FolderEntity>> getFolderList() {
        return mFolderList;
    }


}
