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
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier
import com.nrs.nsnik.notes.dagger.qualifiers.RootFolder
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import dagger.Module
import dagger.Provides
import java.io.File

@Module(includes = arrayOf(ContextModule::class))
class FileModule {

    @ApplicationScope
    @RootFolder
    @Provides
    internal fun provideRootFolder(@ApplicationQualifier context: Context): File {
        return File(context.getExternalFilesDir(context.resources.getString(R.string.folderName)).toString())
    }

}
