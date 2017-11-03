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

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier;
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module(includes = ContextModule.class)
public class GlideModule {

    @ApplicationScope
    @Provides
    RequestManager provideGlide(@NonNull @ApplicationQualifier Context context) {
        return Glide.with(context);
    }

}
