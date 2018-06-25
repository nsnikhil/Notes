/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.RequestManager
import com.crashlytics.android.Crashlytics
import com.github.moduth.blockcanary.BlockCanary
import com.nrs.nsnik.notes.dagger.components.DaggerDatabaseComponent
import com.nrs.nsnik.notes.dagger.components.DaggerFileComponent
import com.nrs.nsnik.notes.dagger.components.DaggerFolderComponent
import com.nrs.nsnik.notes.dagger.components.DaggerGlideComponent
import com.nrs.nsnik.notes.dagger.modules.ContextModule
import com.nrs.nsnik.notes.util.AppBlockCanaryContext
import com.nrs.nsnik.notes.util.DbUtil
import com.nrs.nsnik.notes.util.FileUtil
import com.rollbar.android.Rollbar
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import java.io.File

class MyApplication : Application() {
    companion object {

        init {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        fun getRefWatcher(context: Context): RefWatcher? {
            val application = context.applicationContext as MyApplication
            return application.refWatcher
        }
    }

    private var contextModule: ContextModule = ContextModule(this)
    private var refWatcher: RefWatcher? = null

    lateinit var requestManager: RequestManager
    lateinit var dbUtil: DbUtil
    lateinit var rootFolder: File
    lateinit var fileUtil: FileUtil


    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return super.createStackElementTag(element) + ":" + element.lineNumber
                }
            })
            refWatcher = LeakCanary.install(this)
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            Fabric.with(this, Crashlytics())
        }
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        BlockCanary.install(this, AppBlockCanaryContext()).start()
        Rollbar.init(this, "3b2ad6f009e643fdaf91228cdc54acab", "development")
        moduleSetter()
    }

    private fun moduleSetter() {
        setDatabaseComponent()
        setFileComponent()
        setGlideComponent()
        setFolderComponent()
    }

    private fun setDatabaseComponent() {
        val databaseComponent = DaggerDatabaseComponent.builder().contextModule(contextModule).build()
        dbUtil = databaseComponent.dbUtil
    }

    private fun setFolderComponent() {
        val folderComponent = DaggerFolderComponent.builder().contextModule(contextModule).build()
        rootFolder = folderComponent.rootFolder
    }

    private fun setFileComponent() {
        val fileComponent = DaggerFileComponent.builder().contextModule(contextModule).build()
        fileUtil = fileComponent.fileUtil
    }

    private fun setGlideComponent() {
        val glideComponent = DaggerGlideComponent.builder().contextModule(contextModule).build()
        requestManager = glideComponent.requestManager
    }

}