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

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [(NoteEntity::class), (FolderEntity::class)], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao

    abstract val folderDao: FolderDao
}