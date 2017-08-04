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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.support.design.widget.RxNavigationView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.view.fragments.AboutFragment;
import com.nrs.nsnik.notes.view.fragments.HomeFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;


public class MainActivity extends AppCompatActivity {

    private static final String[] mFragTags = {"home", "starred", "vault", "about"};
    @BindView(R.id.mainToolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.mainDrawerLayout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.mainNaviagtionView)
    NavigationView mNavigationView;
    @BindView(R.id.mainToolBarText)
    TextView mToolbarText;
    private String mCurrentFragment;
    private boolean mIsClicked;

    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.transparentStatusBar);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize(savedInstanceState);
        initializeDrawer();
    }

    private void initialize(Bundle savedInstanceState) {
        setSupportActionBar(mMainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        if (savedInstanceState == null) {
            mCurrentFragment = mFragTags[0];
            getSupportFragmentManager().beginTransaction().add(R.id.mainContainer, new HomeFragment(), mCurrentFragment).commit();
            mToolbarText.setText(getResources().getString(R.string.app_name));
        }
        mCompositeDisposable = new CompositeDisposable();
    }

    /*
    @return true if connected to interned else false
     */
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        }
        return false;
    }

    private void addOnConnection() {
        if (!isConnected()) {
            removeOffConnection();
        }
    }

    private void removeOffConnection() {
        Snackbar.make(mDrawerLayout, getResources().getString(R.string.errorNoInternet), Snackbar.LENGTH_INDEFINITE).setAction(getResources().getString(R.string.errorRetry), view -> addOnConnection()).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMainSearch:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    initialize the navigation drawer by attaching the
    listeners and item click listeners
     */
    private void initializeDrawer() {
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mMainToolbar, R.string.drawerOpen, R.string.drawerClose) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mIsClicked) {
                    mIsClicked = false;
                    performChange();
                }
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mCompositeDisposable.add(RxNavigationView.itemSelections(mNavigationView).subscribe(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.navItem1:
                    if (!mCurrentFragment.equalsIgnoreCase(mFragTags[0])) {
                        mCurrentFragment = mFragTags[0];
                        mIsClicked = true;
                    }
                    break;
                case R.id.navItem2:
                    Toast.makeText(MainActivity.this, "To-Do", Toast.LENGTH_LONG).show();
                    break;
                case R.id.navItem3:
                    Toast.makeText(MainActivity.this, "To-Do", Toast.LENGTH_LONG).show();
                    break;
                case R.id.navItem4:
                    Toast.makeText(MainActivity.this, "To-Do", Toast.LENGTH_LONG).show();
                    break;
                case R.id.navItem5:
                    if (!mCurrentFragment.equalsIgnoreCase(mFragTags[3])) {
                        mCurrentFragment = mFragTags[3];
                        mIsClicked = true;
                    }
                    break;
            }
            mDrawerLayout.closeDrawers();
        }));
    }

    private void performChange() {
        if (mCurrentFragment.equalsIgnoreCase(mFragTags[0])) {
            replaceFragment(new HomeFragment(), mCurrentFragment);
            mToolbarText.setText(getResources().getString(R.string.app_name));
        } else if (mCurrentFragment.equalsIgnoreCase(mFragTags[1])) {
            Toast.makeText(MainActivity.this, "To-Do", Toast.LENGTH_LONG).show();
        } else if (mCurrentFragment.equalsIgnoreCase(mFragTags[2])) {
            Toast.makeText(MainActivity.this, "To-Do", Toast.LENGTH_LONG).show();
        } else if (mCurrentFragment.equalsIgnoreCase(mFragTags[3])) {
            replaceFragment(new AboutFragment(), mCurrentFragment);
            mToolbarText.setText(getResources().getString(R.string.navItem5));
        }
    }

    /*
    @param fragment     the new fragment that will replace the old fragment
    @param tag          the tag for the new fragment
     */
    public void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainContainer, fragment, tag);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.commit();
    }

    private void cleanUp() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUp();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }
}
