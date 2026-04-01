/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
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
import android.content.SharedPreferences
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.RequestManager
import com.google.firebase.FirebaseApp
import com.nrs.nsnik.notes.dagger.components.*
import com.nrs.nsnik.notes.dagger.modules.ContextModule
import com.nrs.nsnik.notes.util.DbUtil
import com.nrs.nsnik.notes.util.FileUtil
import com.nrs.nsnik.notes.util.NetworkUtil
import timber.log.Timber
import java.io.File

class MyApplication : Application() {

    companion object {

        init {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

    }

    private lateinit var contextModule: ContextModule

    lateinit var requestManager: RequestManager
    lateinit var dbUtil: DbUtil
    lateinit var rootFolder: File
    lateinit var fileUtil: FileUtil
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var networkUtil: NetworkUtil

    override fun onCreate() {
        super.onCreate()
        contextModule = ContextModule(this)

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Timber.e(e, "Firebase init failed")
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return super.createStackElementTag(element) + ":" + element.lineNumber
                }
            })
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
        moduleSetter()
    }

    private fun moduleSetter() {
        setDatabaseComponent()
        setFileComponent()
        setGlideComponent()
        setFolderComponent()
        setSharedPrefComponent()
        setNetworkModule()
    }

    private fun setDatabaseComponent() {
        dbUtil = DaggerDatabaseComponent.builder().contextModule(contextModule).build().dbUtil
    }

    private fun setFolderComponent() {
        rootFolder = DaggerFolderComponent.builder().contextModule(contextModule).build().rootFolder
    }

    private fun setFileComponent() {
        fileUtil = DaggerFileComponent.builder().contextModule(contextModule).build().fileUtil
    }

    private fun setGlideComponent() {
        requestManager = DaggerGlideComponent.builder().contextModule(contextModule).build().requestManager
    }

    private fun setSharedPrefComponent() {
        sharedPreferences = DaggerSharedPrefComponent.builder().contextModule(contextModule).build().sharedPreferences
    }

    private fun setNetworkModule() {
        networkUtil = DaggerNetworkComponent.create().getNetworkUtil()
    }

}