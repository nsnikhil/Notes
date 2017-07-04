package com.nrs.nsnik.notes;

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

import com.crashlytics.android.Crashlytics;
import com.nrs.nsnik.notes.fragments.AboutFragment;
import com.nrs.nsnik.notes.fragments.HomeFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] mFragTags = {"home", "starred", "recent", "about"};
    @BindView(R.id.mainToolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.mainDrawerLayout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.mainNaviagtionView)
    NavigationView mNavigationView;
    @BindView(R.id.mainToolBarText)
    TextView mToolbarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.transparentStatusBar);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
        initializeDrawer();
        getSupportFragmentManager().beginTransaction().add(R.id.mainContainer, new HomeFragment(), mFragTags[0]).commit();
    }

    /*
    @return true if connected to interned else flase
     */
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void addOnConnection() {
        if (!isConnected()) {
            removeOffConnection();
        }
    }

    private void removeOffConnection() {
        Snackbar.make(mDrawerLayout, "No Internet", Snackbar.LENGTH_INDEFINITE).setAction("Retry", view -> addOnConnection()).show();
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
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.navItem1:
                    if (getSupportFragmentManager().findFragmentByTag(mFragTags[0]) == null) {
                        replaceFragment(new HomeFragment(), mFragTags[0]);
                        drawerAction(0);
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
                    if (getSupportFragmentManager().findFragmentByTag(mFragTags[3]) == null) {
                        replaceFragment(new AboutFragment(), mFragTags[3]);
                        drawerAction(3);
                    }
                    break;
            }
            return true;
        });
    }

    /*
    @param key  the position of the navigation menu that was clicked
     */
    private void drawerAction(int key) {
        MenuItem notes = mNavigationView.getMenu().getItem(0).setChecked(false);
        MenuItem starred = mNavigationView.getMenu().getItem(1).setChecked(false);
        MenuItem recent = mNavigationView.getMenu().getItem(2).setChecked(false);
        MenuItem about = mNavigationView.getMenu().getItem(3).setChecked(false);
        switch (key) {
            case 0:
                notes.setChecked(true);
                mToolbarText.setText(getResources().getString(R.string.app_name));
                break;
            case 1:
                starred.setChecked(true);
                break;
            case 2:
                recent.setChecked(true);
                break;
            case 3:
                about.setChecked(true);
                mToolbarText.setText(getResources().getString(R.string.navItem5));
                break;
        }
    }

    private void initialize() {
        setSupportActionBar(mMainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbarText.setText(getResources().getString(R.string.app_name));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }
}
