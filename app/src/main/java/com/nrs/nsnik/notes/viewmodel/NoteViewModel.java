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
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.util.DbUtil;

import java.util.List;


public class NoteViewModel extends AndroidViewModel {

    private DbUtil mDbUtil;
    private LiveData<List<NoteEntity>> mNoteList;

    public NoteViewModel(Application application) {
        super(application);
        mDbUtil = ((MyApplication) application).getDbUtil();
        mNoteList = mDbUtil.getNoteList();
    }

    public LiveData<List<NoteEntity>> getNoteList() {
        return this.mNoteList;
    }

    public void insertNote(NoteEntity... noteEntities) {
        mDbUtil.insertNote(noteEntities);
    }

    public void updateNote(NoteEntity... noteEntities) {
        mDbUtil.updateNote(noteEntities);
    }

    public void deleteNote(NoteEntity... noteEntities) {
        mDbUtil.deleteNote(noteEntities);
    }

    public void deleteNoteByFolderName(String folderName) {
        mDbUtil.deleteNoteByFolderName(folderName);
    }

    public LiveData<NoteEntity> getNoteById(int id) {
        return mDbUtil.getNoteById(id);
    }

    public LiveData<List<NoteEntity>> getNoteByFolderName(String folderName) {
        return mDbUtil.getNoteByFolderName(folderName);
    }

    public LiveData<List<NoteEntity>> searchNote(String query) {
        return mDbUtil.searchNote(query);
    }

    public LiveData<List<NoteEntity>> getNoteByPin(int isPinned) {
        return mDbUtil.getNotesByPin(isPinned);
    }

    public LiveData<List<NoteEntity>> getNoteByLock(int isLocked) {
        return mDbUtil.getNotesByLock(isLocked);
    }

    public LiveData<List<NoteEntity>> getNoteByColor(String color) {
        return mDbUtil.getNotesByColor(color);
    }
}
