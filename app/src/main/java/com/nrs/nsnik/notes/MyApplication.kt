/*
 *     Credit Card Security V1  Copyright (C) 2018  sid-sun
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.RequestManager
import com.crashlytics.android.Crashlytics
import com.github.moduth.blockcanary.BlockCanary
import com.nrs.nsnik.notes.dagger.components.*
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
    lateinit var sharedPreferences: SharedPreferences


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
        setSharedPrefComponent()
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

    private fun setSharedPrefComponent(){
        val sharedPrefComponent = DaggerSharedPrefComponent.builder().contextModule(contextModule).build()
        sharedPreferences = sharedPrefComponent.sharedPreferences
    }
}