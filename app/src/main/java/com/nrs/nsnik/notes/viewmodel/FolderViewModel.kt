/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.data.FolderEntity
import com.nrs.nsnik.notes.util.DbUtil

class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val mDbUtil: DbUtil?
    private val folderList: LiveData<List<FolderEntity>>

    init {
        mDbUtil = (application as MyApplication).dbUtil
        folderList = mDbUtil.folderList
    }

    fun insertFolder(vararg folderEntities: FolderEntity) {
        mDbUtil!!.insertFolder(*folderEntities)
    }

    fun updateFolder(vararg folderEntities: FolderEntity) {
        mDbUtil!!.updateFolder(*folderEntities)
    }

    fun deleteFolder(vararg folderEntities: FolderEntity) {
        mDbUtil!!.deleteFolder(*folderEntities)
    }

    fun deleteFolderByName(name: String) {
        mDbUtil!!.deleteFolderByName(name)
    }

    fun deleteFolderByParent(parentFolderName: String) {
        mDbUtil!!.deleteFolderByParent(parentFolderName)
    }

    fun getFolderById(id: Int): LiveData<FolderEntity> {
        return mDbUtil!!.getFolderById(id)
    }

    fun getFolderByName(name: String): LiveData<FolderEntity> {
        return mDbUtil!!.getFolderByName(name)
    }

    fun searchFolder(query: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.searchFolder(query)
    }

    fun getFolderByParent(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByParent(parentFolderName)
    }

    fun getFolderByParentNoPinNoLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByParentNoPinNoLock(parentFolderName)
    }

    fun getFolderByParentPinNoLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByParentPinNoLock(parentFolderName)
    }

    fun getFolderByParentNoPinLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByParentNoPinLock(parentFolderName)
    }

    fun getFolderByParentPinLock(parentFolderName: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByParentPinLock(parentFolderName)
    }

    fun getFolderByPin(isPinned: Int): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByPin(isPinned)

    }

    fun getFolderByLock(isLocked: Int): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByLock(isLocked)

    }

    fun getFolderByColor(color: String): LiveData<List<FolderEntity>> {
        return mDbUtil!!.getFolderByColor(color)
    }
}
