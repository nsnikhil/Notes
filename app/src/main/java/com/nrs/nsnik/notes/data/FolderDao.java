/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FolderDao {

    @Query("SELECT * FROM FolderEntity")
    LiveData<List<FolderEntity>> getFoldersList();

    @Query("SELECT * FROM FolderEntity WHERE mUid = :id")
    LiveData<FolderEntity> getFolder(int id);

    @Query("SELECT * FROM FolderEntity WHERE mFolderName = :folderName")
    LiveData<FolderEntity> getFolderByName(String folderName);

    @Query("SELECT * FROM FolderEntity WHERE mFolderName LIKE :query")
    LiveData<List<FolderEntity>> getFolderByQuery(String query);

    @Query("SELECT * FROM FolderEntity WHERE mParentFolderName = :parentFolder")
    LiveData<List<FolderEntity>> getFolderByParent(String parentFolder);

    @Query("SELECT * FROM FolderEntity WHERE mIsPinned = :isPinned")
    LiveData<List<FolderEntity>> getFolderByPin(int isPinned);

    @Query("SELECT * FROM FolderEntity WHERE mIsLocked = :isLocked")
    LiveData<List<FolderEntity>> getFolderByLock(int isLocked);

    @Query("SELECT * FROM FolderEntity WHERE mColor = :color")
    LiveData<List<FolderEntity>> getFolderByColor(String color);

    @Insert
    long[] insertFolders(FolderEntity... folderEntities);

    @Delete
    void deleteFolders(FolderEntity... folderEntities);

    //DELETE USING FOLDER NAME
    @Query("DELETE FROM FolderEntity WHERE mFolderName = :folderName")
    void deleteFolderByName(String folderName);

    //DELETE USING PARENT FOLDER NAME
    @Query("DELETE FROM FolderEntity WHERE mParentFolderName = :parentFolderName")
    void deleteFolderByParent(String parentFolderName);

    @Update
    int updateFolders(FolderEntity... folderEntities);

}