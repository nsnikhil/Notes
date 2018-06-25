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
interface NoteDao {

    @get:Query("SELECT * FROM NoteEntity")
    val notesList: LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE uid = :id")
    fun getNote(id: Int): LiveData<NoteEntity>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName")
    fun getNoteByFolderName(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 0 And pinned = 0")
    fun getNotesByFolderNameNotPinnedNotLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 0 And pinned = 1")
    fun getNotesByFolderNamePinnedNotLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 1 And pinned = 0")
    fun getNotesByFolderNameNotPinnedLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE folderName = :folderName AND locked = 1 And pinned = 1")
    fun getNotesByFolderNamePinnedLocked(folderName: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE title LIKE :query")
    fun getNoteByQuery(query: String): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE pinned = :isPinned")
    fun getNoteByPin(isPinned: Int): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE locked = :isLocked")
    fun getNoteByLock(isLocked: Int): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE color = :color")
    fun getNoteByColor(color: String): LiveData<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotes(vararg noteEntity: NoteEntity): LongArray

    @Delete
    fun deleteNotes(vararg noteEntities: NoteEntity)

    @Query("DELETE FROM NoteEntity WHERE folderName = :folderName")
    fun deleteNoteByFolderName(folderName: String)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(vararg noteEntities: NoteEntity): Int
}

