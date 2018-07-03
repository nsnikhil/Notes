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

package com.nrs.nsnik.notes.view

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.test.espresso.IdlingResource
import com.nrs.nsnik.notes.BuildConfig
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.util.idlingResource.SimpleIdlingResource
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var mIdlingResource: SimpleIdlingResource? = null
    private var isOnListFragment: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
    }

    override fun onSupportNavigateUp() = findNavController(R.id.mainNavHost).navigateUp()

    private fun initialize() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24px)
        }

        val controller = findNavController(R.id.mainNavHost)

        controller.addOnNavigatedListener { navController, destination ->
            isOnListFragment = destination.id == R.id.navItemNotes
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

        mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navItemNotes -> {
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemSettings -> {
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    findNavController(R.id.mainNavHost).navigate(R.id.navItemSettings)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemAbout -> {
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    findNavController(R.id.mainNavHost).navigate(R.id.navItemAbout)
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isOnListFragment)
                    mainDrawerLayout.openDrawer(GravityCompat.START)
            }
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
        if (BuildConfig.DEBUG) {
            val refWatcher = MyApplication.getRefWatcher(this)
            refWatcher!!.watch(this)
        }
    }
}