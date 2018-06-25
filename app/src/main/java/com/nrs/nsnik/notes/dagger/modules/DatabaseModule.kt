/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.dagger.modules


import android.content.Context
import androidx.room.Room
import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier
import com.nrs.nsnik.notes.dagger.qualifiers.DatabaseName
import com.nrs.nsnik.notes.dagger.qualifiers.DefaultFolder
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import com.nrs.nsnik.notes.data.NotesDatabase
import dagger.Module
import dagger.Provides

@Module(includes = arrayOf(ContextModule::class))
class DatabaseModule {

    internal val databaseName: String
        @Provides
        @DatabaseName
        @ApplicationScope
        get() = DATABASE_NAME

    internal val defaultFolderName: String
        @DefaultFolder
        @ApplicationScope
        @Provides
        get() = DEFAULT_FOLDER_NAME

    @Provides
    @ApplicationScope
    internal fun getNoteDatabase(@ApplicationQualifier context: Context, @DatabaseName @ApplicationScope databaseName: String): NotesDatabase {
        return Room.databaseBuilder(context, NotesDatabase::class.java, databaseName).build()
    }

    companion object {

        private val DATABASE_NAME = "notesDb"
        private val DEFAULT_FOLDER_NAME = "noFolder"
    }

}
