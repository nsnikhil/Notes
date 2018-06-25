/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FolderDao {

    @get:Query("SELECT * FROM FolderEntity")
    val foldersList: LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE uid = :id")
    fun getFolder(id: Int): LiveData<FolderEntity>

    @Query("SELECT * FROM FolderEntity WHERE folderName = :folderName")
    fun getFolderByName(folderName: String): LiveData<FolderEntity>

    @Query("SELECT * FROM FolderEntity WHERE folderName LIKE :query")
    fun getFolderByQuery(query: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder")
    fun getFolderByParent(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 0 AND locked = 0")
    fun getFolderByParentNoPinNoLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 1 AND locked = 0")
    fun getFolderByParentPinNoLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 0 AND locked = 1")
    fun getFolderByParentNoPinLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE parentFolderName = :parentFolder AND pinned = 1 AND locked = 1")
    fun getFolderByParentPinLock(parentFolder: String): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE pinned = :isPinned")
    fun getFolderByPin(isPinned: Int): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE locked = :isLocked")
    fun getFolderByLock(isLocked: Int): LiveData<List<FolderEntity>>

    @Query("SELECT * FROM FolderEntity WHERE color = :color")
    fun getFolderByColor(color: String): LiveData<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFolders(vararg folderEntities: FolderEntity): LongArray

    @Delete
    fun deleteFolders(vararg folderEntities: FolderEntity)

    @Query("DELETE FROM FolderEntity WHERE folderName = :folderName")
    fun deleteFolderByName(folderName: String)

    @Query("DELETE FROM FolderEntity WHERE parentFolderName = :parentFolderName")
    fun deleteFolderByParent(parentFolderName: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateFolders(vararg folderEntities: FolderEntity): Int

}
