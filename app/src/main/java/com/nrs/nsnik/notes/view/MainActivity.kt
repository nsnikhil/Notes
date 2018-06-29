/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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
        setupActionBarWithNavController(this, controller, mainDrawerLayout)

        mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navItemNotes -> {
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemStarred -> {
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemVault -> {
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemSettings -> {
                    findNavController(R.id.mainNavHost).navigate(R.id.navItemSettings)
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    return@setNavigationItemSelectedListener true
                }
                R.id.navItemAbout -> {
                    findNavController(R.id.mainNavHost).navigate(R.id.navItemAbout)
                    mainDrawerLayout.closeDrawer(Gravity.START)
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> mainDrawerLayout.openDrawer(GravityCompat.START)
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