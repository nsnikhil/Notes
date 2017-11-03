/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.dagger.modules;


import android.arch.persistence.room.Room;
import android.content.Context;

import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier;
import com.nrs.nsnik.notes.dagger.qualifiers.DatabaseName;
import com.nrs.nsnik.notes.dagger.qualifiers.DefaultFolder;
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope;
import com.nrs.nsnik.notes.data.NotesDatabase;

import org.jetbrains.annotations.NotNull;

import dagger.Module;
import dagger.Provides;

@Module(includes = {ContextModule.class})
public class DatabaseModule {

    private static final String DATABASE_NAME = "notesDb";
    private final String DEFAULT_FOLDER_NAME = "noFolder";

    @NotNull
    @Provides
    @DatabaseName
    @ApplicationScope
    String getDatabaseName() {
        return DATABASE_NAME;
    }

    @NotNull
    @DefaultFolder
    @ApplicationScope
    @Provides
    String getDefaultFolderName() {
        return DEFAULT_FOLDER_NAME;
    }

    @NotNull
    @Provides
    @ApplicationScope
    NotesDatabase getNoteDatabase(@NotNull @ApplicationQualifier Context context, @NotNull @DatabaseName @ApplicationScope String databaseName) {
        return Room.databaseBuilder(context, NotesDatabase.class, databaseName).build();
    }

}
