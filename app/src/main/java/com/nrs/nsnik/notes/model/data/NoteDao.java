/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.model.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM NoteEntity")
    List<NoteEntity> getNotes();

    @Query("SELECT * FROM NoteEntity WHERE mUid = :id")
    NoteEntity getNote(int id);

    @Query("SELECT * FROM NoteEntity WHERE mFolderName = :folderName")
    NoteEntity getNoteByFolderName(String folderName);

    @Query("SELECT * FROM NoteEntity WHERE mTitle LIKE :query")
    NoteEntity getNoteByQuery(String query);

    @Query("SELECT * FROM NoteEntity WHERE mIsPinned = :isPinned")
    NoteEntity getNoteByPin(int isPinned);

    @Query("SELECT * FROM NoteEntity WHERE mIsLocked = :isLocked")
    NoteEntity getNoteByLock(int isLocked);

    @Query("SELECT * FROM NoteEntity WHERE mColor = :color")
    NoteEntity getNoteByColor(String color);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertNotes(NoteEntity... noteEntity);

    @Delete
    void deleteNotes(NoteEntity... noteEntities);

    //DELETE
    @Query("DELETE FROM NoteEntity WHERE mFolderName = :folderName")
    void deletNoteBYFolderName(String folderName);

    @Update
    int updateNote(NoteEntity... noteEntities);

    //ONE UPDATE FUNCTION IS LEFT WITH CUSTOM PARAMETER WHICH IS UPDATE VIA FOLDER NAME
}

