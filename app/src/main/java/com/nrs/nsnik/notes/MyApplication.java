/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.nrs.nsnik.notes.dagger.components.DaggerGlideComponent;
import com.nrs.nsnik.notes.dagger.components.DaggerRootFolderComponent;
import com.nrs.nsnik.notes.dagger.components.GlideComponent;
import com.nrs.nsnik.notes.dagger.components.RootFolderComponent;
import com.nrs.nsnik.notes.dagger.modules.ContextModule;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MyApplication extends Application {

    @Inject
    File mRootFolder;
    private RefWatcher refWatcher;
    private GlideComponent mGlideComponent;

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication application = (MyApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        if (BuildConfig.DEBUG) {
            refWatcher = LeakCanary.install(this);
        }
        moduleSetter();
    }

    private void moduleSetter() {
        setGlideComponent();
        setRootFolderComponent();
    }

    private void setGlideComponent() {
        mGlideComponent = DaggerGlideComponent.builder().contextModule(new ContextModule(this)).build();
    }

    private void setRootFolderComponent() {
        RootFolderComponent rootFolderComponent = DaggerRootFolderComponent.builder().contextModule(new ContextModule(this)).build();
        rootFolderComponent.inject(this);
    }

    public File getRootFolder() {
        return mRootFolder;
    }

    public GlideComponent getGlideComponent() {
        return mGlideComponent;
    }
}
