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
import com.nrs.nsnik.notes.data.NoteEntity
import com.nrs.nsnik.notes.util.DbUtil


class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val mDbUtil: DbUtil?
    private val noteList: LiveData<List<NoteEntity>>

    init {
        mDbUtil = (application as MyApplication).dbUtil
        noteList = mDbUtil.noteList
    }

    fun insertNote(vararg noteEntities: NoteEntity) {
        mDbUtil!!.insertNote(*noteEntities)
    }

    fun updateNote(vararg noteEntities: NoteEntity) {
        mDbUtil!!.updateNote(*noteEntities)
    }

    fun deleteNote(vararg noteEntities: NoteEntity) {
        mDbUtil!!.deleteNote(*noteEntities)
    }

    fun deleteNoteByFolderName(folderName: String) {
        mDbUtil!!.deleteNoteByFolderName(folderName)
    }

    fun getNoteById(id: Int): LiveData<NoteEntity> {
        return mDbUtil!!.getNoteById(id)
    }

    fun getNoteByFolderName(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNoteByFolderName(folderName)
    }

    fun getNoteByFolderNameNoPinNoLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNoteByFolderNameNoPinNoLock(folderName)
    }

    fun getNoteByFolderNamePinNoLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNoteByFolderNamePinNoLock(folderName)
    }

    fun getNoteByFolderNameNoPinLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNoteByFolderNameNoPinLock(folderName)
    }

    fun getNoteByFolderNamePinLock(folderName: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNoteByFolderNamePinLock(folderName)
    }

    fun searchNote(query: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.searchNote(query)
    }

    fun getNoteByPin(isPinned: Int): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNotesByPin(isPinned)
    }

    fun getNoteByLock(isLocked: Int): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNotesByLock(isLocked)
    }

    fun getNoteByColor(color: String): LiveData<List<NoteEntity>> {
        return mDbUtil!!.getNotesByColor(color)
    }
}
