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
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.bumptech.glide.RequestManager;
import com.crashlytics.android.Crashlytics;
import com.github.moduth.blockcanary.BlockCanary;
import com.nrs.nsnik.notes.dagger.components.DaggerDatabaseComponent;
import com.nrs.nsnik.notes.dagger.components.DaggerFileComponent;
import com.nrs.nsnik.notes.dagger.components.DaggerFolderComponent;
import com.nrs.nsnik.notes.dagger.components.DaggerGlideComponent;
import com.nrs.nsnik.notes.dagger.components.DatabaseComponent;
import com.nrs.nsnik.notes.dagger.components.FileComponent;
import com.nrs.nsnik.notes.dagger.components.FolderComponent;
import com.nrs.nsnik.notes.dagger.components.GlideComponent;
import com.nrs.nsnik.notes.dagger.modules.ContextModule;
import com.nrs.nsnik.notes.util.AppBlockCanaryContext;
import com.nrs.nsnik.notes.util.DbUtil;
import com.nrs.nsnik.notes.util.FileUtil;
import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class MyApplication extends Application {

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private ContextModule mContextModule;
    private RefWatcher refWatcher;
    private RequestManager mRequestManager;
    private DbUtil mDbUtil;
    private File mRootFolder;
    private FileUtil mFileUtil;

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
                protected String createStackElementTag(@NonNull StackTraceElement element) {
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
        BlockCanary.install(this, new AppBlockCanaryContext()).start();
        Rollbar.init(this, "3b2ad6f009e643fdaf91228cdc54acab", "development");
        moduleSetter();
    }

    private void moduleSetter() {
        setContextModule();
        setDatabaseComponent();
        setFileComponent();
        setGlideComponent();
        setFolderComponent();
    }

    private void setContextModule() {
        mContextModule = new ContextModule(this);
    }

    private void setDatabaseComponent() {
        DatabaseComponent databaseComponent = DaggerDatabaseComponent.builder().contextModule(mContextModule).build();
        mDbUtil = databaseComponent.getDbUtil();
    }

    private void setFolderComponent() {
        FolderComponent folderComponent = DaggerFolderComponent.builder().contextModule(mContextModule).build();
        mRootFolder = folderComponent.getRootFolder();
    }

    private void setFileComponent() {
        FileComponent fileComponent = DaggerFileComponent.builder().contextModule(mContextModule).build();
        mFileUtil = fileComponent.getFileUtil();
    }

    private void setGlideComponent() {
        GlideComponent glideComponent = DaggerGlideComponent.builder().contextModule(mContextModule).build();
        mRequestManager = glideComponent.getRequestManager();
    }

    public File getRootFolder() {
        return mRootFolder;
    }

    public FileUtil getFileUtil() {
        return mFileUtil;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public DbUtil getDbUtil() {
        return mDbUtil;
    }

}