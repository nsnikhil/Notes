/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.util.DbUtil;

import java.util.List;

public class FolderViewModel extends AndroidViewModel {

    private final DbUtil mDbUtil;
    private LiveData<List<FolderEntity>> mFolderList;


    public FolderViewModel(Application application) {
        super(application);
        mDbUtil = ((MyApplication) application).getDbUtil();
        mFolderList = mDbUtil.getFolderList();
    }

    public LiveData<List<FolderEntity>> getFolderList() {
        return mFolderList;
    }

    public void insertFolder(FolderEntity... folderEntities) {
        mDbUtil.insertFolder(folderEntities);
    }

    public void updateFolder(FolderEntity... folderEntities) {
        mDbUtil.updateFolder(folderEntities);
    }

    public void deleteFolder(FolderEntity... folderEntities) {
        mDbUtil.deleteFolder(folderEntities);
    }

    public void deleteFolderByName(String name) {
        mDbUtil.deleteFolderByName(name);
    }

    public void deleteFolderByParent(String parentFolderName) {
        mDbUtil.deleteFolderByParent(parentFolderName);
    }

    public LiveData<FolderEntity> getFolderById(int id) {
        return mDbUtil.getFolderById(id);
    }

    public LiveData<FolderEntity> getFolderByName(String name) {
        return mDbUtil.getFolderByName(name);
    }

    public LiveData<List<FolderEntity>> searchFolder(String query) {
        return mDbUtil.searchFolder(query);
    }

    public LiveData<List<FolderEntity>> getFolderByParent(String parentFolderName) {
        return mDbUtil.getFolderByParent(parentFolderName);
    }

    public LiveData<List<FolderEntity>> getFolderByParentNoPinNoLock(String parentFolderName) {
        return mDbUtil.getFolderByParentNoPinNoLock(parentFolderName);
    }

    public LiveData<List<FolderEntity>> getFolderByParentPinNoLock(String parentFolderName) {
        return mDbUtil.getFolderByParentPinNoLock(parentFolderName);
    }

    public LiveData<List<FolderEntity>> getFolderByParentNoPinLock(String parentFolderName) {
        return mDbUtil.getFolderByParentNoPinLock(parentFolderName);
    }

    public LiveData<List<FolderEntity>> getFolderByParentPinLock(String parentFolderName) {
        return mDbUtil.getFolderByParentPinLock(parentFolderName);
    }

    public LiveData<List<FolderEntity>> getFolderByPin(int isPinned) {
        return mDbUtil.getFolderByPin(isPinned);

    }

    public LiveData<List<FolderEntity>> getFolderByLock(int isLocked) {
        return mDbUtil.getFolderByLock(isLocked);

    }

    public LiveData<List<FolderEntity>> getFolderByColor(String color) {
        return mDbUtil.getFolderByColor(color);
    }
}
