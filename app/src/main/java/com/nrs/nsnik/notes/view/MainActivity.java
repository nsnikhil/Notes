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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
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
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.util.idlingResource.SimpleIdlingResource;
import com.nrs.nsnik.notes.view.fragments.AboutFragment;
import com.nrs.nsnik.notes.view.fragments.HomeFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;


public class MainActivity extends AppCompatActivity {

    private static final String[] mFragTags = {"home", "starred", "vault", "about"};
    @Nullable
    @BindView(R.id.mainDrawerLayout)
    DrawerLayout mDrawerLayout;
    @Nullable
    @BindView(R.id.mainNavigationView)
    NavigationView mNavigationView;
    @Nullable
    @BindView(R.id.mainToolbar)
    Toolbar mMainToolbar;
    @Nullable
    @BindView(R.id.mainToolbarText)
    TextView mToolbarText;
    private String mCurrentFragment;
    private boolean mIsClicked;
    private CompositeDisposable mCompositeDisposable;

    @Nullable
    private SimpleIdlingResource mIdlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.transparentStatusBar);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize(savedInstanceState);
        initializeDrawer();
        getIdlingResource();
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    private void initialize(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState == null && mToolbarText != null) {
            setSupportActionBar(mMainToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            mCurrentFragment = mFragTags[0];

            getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, new HomeFragment(), mCurrentFragment).commit();
            mToolbarText.setText(getResources().getString(R.string.app_name));
        }
        mCompositeDisposable = new CompositeDisposable();
    }

    /**
     * initialize the navigation drawer by attaching the
     * listeners and item click listeners
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
        if (mDrawerLayout != null) {
            mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        }
        actionBarDrawerToggle.syncState();
        if (mNavigationView != null) {
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }
        if (mNavigationView != null) {
            mCompositeDisposable.add(RxNavigationView.itemSelections(mNavigationView).subscribe(menuItem -> {
                if (mIdlingResource != null) {
                    mIdlingResource.setIdleState(false);
                }
                switch (menuItem.getItemId()) {
                    case R.id.navItem1:
                        if (mCurrentFragment != null && !mCurrentFragment.equalsIgnoreCase(mFragTags[0])) {
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
                        if (mCurrentFragment != null && !mCurrentFragment.equalsIgnoreCase(mFragTags[3])) {
                            mCurrentFragment = mFragTags[3];
                            mIsClicked = true;
                        }
                        break;
                }
                mDrawerLayout.closeDrawers();
            }));
        }
    }

    private void performChange() {
        if (mToolbarText != null) {
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
        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(true);
        }
    }

    /**
     * @param fragment the new fragment that will replace the old fragment
     * @param tag      the tag for the new fragment
     */
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.mainContainer, fragment, tag)
                .commit();
    }

    private void cleanUp() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMainSearch:
                break;
        }
        return super.onOptionsItemSelected(item);
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
