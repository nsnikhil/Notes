/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.model.dagger.components.ApplicationComponent;
import com.nrs.nsnik.notes.model.dagger.components.DaggerApplicationComponent;
import com.nrs.nsnik.notes.model.dagger.components.DaggerGlideComponent;
import com.nrs.nsnik.notes.model.dagger.components.GlideComponent;
import com.nrs.nsnik.notes.model.dagger.modules.ContextModule;
import com.nrs.nsnik.notes.model.data.TableHelper;
import com.nrs.nsnik.notes.util.DatabaseOperations;
import com.nrs.nsnik.notes.util.FileOperation;
import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MyApplication extends Application {

    @Inject
    File mRootFolder;

    @Inject
    FileOperation mFileOperations;

    @Inject
    DatabaseOperations mDatabaseOperations;

    @Inject
    TableHelper mTableHelper;

    private RefWatcher refWatcher;
    private GlideComponent mGlideComponent;

    public static RefWatcher getRefWatcher(@NonNull Context context) {
        MyApplication application = (MyApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @NonNull
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
            refWatcher = LeakCanary.install(this);
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            Fabric.with(this, new Crashlytics());
        }
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        Rollbar.init(this, "3b2ad6f009e643fdaf91228cdc54acab", "development");
        moduleSetter();
    }

    private void moduleSetter() {
        setGlideComponent();
        setAppComponent();
    }

    private void setAppComponent() {
        ApplicationComponent component = DaggerApplicationComponent.builder().contextModule(new ContextModule(this)).build();
        component.injectInApplication(this);
    }

    private void setGlideComponent() {
        mGlideComponent = DaggerGlideComponent.builder().contextModule(new ContextModule(this)).build();
    }

    public File getRootFolder() {
        return mRootFolder;
    }

    public FileOperation getFileOperations() {
        return mFileOperations;
    }

    public DatabaseOperations getDatabaseOperations() {
        return mDatabaseOperations;
    }

    public GlideComponent getGlideComponent() {
        return mGlideComponent;
    }

    public TableHelper getTableHelper() {
        return mTableHelper;
    }
}
