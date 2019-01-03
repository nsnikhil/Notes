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

package com.nrs.nsnik.notes.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.test.espresso.IdlingResource
import com.nrs.nsnik.notes.BuildConfig
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.util.idlingResource.SimpleIdlingResource
import io.branch.referral.Branch
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var mIdlingResource: SimpleIdlingResource? = null
    private var isOnListFragment: Boolean = true

    companion object {
        private const val SearchIntentAction: String = "com.nrs.nsnik.notes.StartSearch"
        private const val NewNoteIntentAction: String = "com.nrs.nsnik.notes.OpenNewNotesFragment"
    }

    override fun onStart() {
        super.onStart()
        Branch.getInstance().initSession({ referringParams, error ->
            Timber.d(if (error == null) referringParams.toString() else error.message)
        }, this.intent.data, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
        handleIntent()
    }

//override fun onSupportNavigateUp() = findNavController(R.id.mainNavHost).navigateUp()

    override fun onResume() {
        super.onResume()
        if (Branch.isAutoDeepLinkLaunch(this)) {
            try {
                val autoDeeplinkedValue = Branch.getInstance().latestReferringParams.getString("noteTitle")
                Timber.d("Launched by Branch on auto deep linking!\n\n$autoDeeplinkedValue")
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        } else {
            Timber.d("Launched by normal application flow")
        }
    }

    private fun initialize() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.apply {
            //            setDisplayHomeAsUpEnabled(true)
//            setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24px)
        }

        val controller = findNavController(R.id.mainNavHost)


        controller.addOnDestinationChangedListener { navController, destination, arguments ->
            isOnListFragment = destination.id == R.id.navItemNotes
            mainToolbar.visibility = if (destination.id == R.id.introFragment) View.GONE else View.VISIBLE
            mainDrawerLayout.setDrawerLockMode(if (destination.id == R.id.navItemNotes) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            mainNavigationView.setCheckedItem(
                    when (destination.id) {
                        R.id.navItemNotes -> R.id.navItemNotes
                        R.id.navItemSettings -> R.id.navItemSettings
                        R.id.navItemAbout -> R.id.navItemAbout
                        else -> R.id.navItemNotes
                    }
            )
        }

        setupActionBarWithNavController(this, controller, mainDrawerLayout)

        AppBarConfiguration(controller.graph, mainDrawerLayout)

        mainNavigationView.setupWithNavController(controller)

        mainToolbar.setupWithNavController(controller)

        mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navItemNotes -> {
                    mainDrawerLayout.closeDrawer(GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemSettings -> {
                    mainDrawerLayout.closeDrawer(GravityCompat.START)
                    findNavController(R.id.mainNavHost).navigate(R.id.navItemSettings)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemAbout -> {
                    mainDrawerLayout.closeDrawer(GravityCompat.START)
                    findNavController(R.id.mainNavHost).navigate(R.id.navItemAbout)
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener false
            }
        }
    }

    private fun handleIntent() {
        val intent = intent
        val action = intent.action
        val type = intent.type

        when (action) {
            Intent.ACTION_SEND -> {
                if (type != null)
                    if ("text/plain" == type) handleSendText(intent) else if (type.startsWith("image/")) handleSendImage(intent)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (type != null)
                    if (type.startsWith("image/")) handleSendMultipleImages(intent)
            }
            NewNoteIntentAction -> findNavController(R.id.mainNavHost).navigate(R.id.newNoteFragment)
            SearchIntentAction -> findNavController(R.id.mainNavHost).navigate(R.id.searchFragment)
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            val bundle = Bundle()
            bundle.putString(resources.getString(R.string.bundleReceiveIntentText), sharedText)
            findNavController(R.id.mainNavHost).navigate(R.id.newNoteFragment, bundle)
        }
    }

    private fun handleSendImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
        val bundle = Bundle()
        bundle.putParcelable(resources.getString(R.string.bundleReceiveIntentImage), imageUri)
        findNavController(R.id.mainNavHost).navigate(R.id.newNoteFragment, bundle)
    }

    private fun handleSendMultipleImages(intent: Intent) {
        val imageUris = intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
        if (imageUris != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(resources.getString(R.string.bundleReceiveIntentImageList), imageUris)
            findNavController(R.id.mainNavHost).navigate(R.id.newNoteFragment, bundle)
        }
    }

    override fun onBackPressed() {
        if (mainDrawerLayout.isDrawerOpen(GravityCompat.START)) mainDrawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (isOnListFragment) mainDrawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    @VisibleForTesting
    @NonNull
    fun getIdlingResource(): IdlingResource {
        if (mIdlingResource == null) mIdlingResource = SimpleIdlingResource()
        return mIdlingResource as IdlingResource
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) MyApplication.getRefWatcher(this)?.watch(this)
    }
}