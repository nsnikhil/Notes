package com.nrs.nsnik.notes;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.nrs.nsnik.notes.fragments.HomeFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    private static final String[] mFragTags = {"home", "starred", "recent"};
    @BindView(R.id.mainToolbar) Toolbar mMainToolbar;
    @BindView(R.id.mainDrawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.mainNaviagtionView) NavigationView mNavigationView;
    @BindView(R.id.mainToolBarText)TextView mToolbarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTheme(R.style.transparentStatusBar);
        }
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        addOnConnection();
    }

    private void addOnConnection(){
        if(isConnected()){
            initialize();
            initializeDrawer();
            getSupportFragmentManager().beginTransaction().add(R.id.mainContainer, new HomeFragment(), mFragTags[0]).commit();
        }else {
            removeOffConnection();
        }
    }

    private void removeOffConnection(){
        Snackbar.make(mDrawerLayout,"No Internet",Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addOnConnection();
            }
        }).show();
    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                }
                return true;
            }
        });
    }

    private void drawerAction(int key) {
        MenuItem notes = mNavigationView.getMenu().getItem(0).setChecked(false);
        MenuItem starred = mNavigationView.getMenu().getItem(1).setChecked(false);
        MenuItem recants = mNavigationView.getMenu().getItem(2).setChecked(false);
        switch (key) {
            case 0:
                notes.setChecked(true);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                }
                break;
            case 1:
                starred.setChecked(true);
                break;
            case 2:
                recants.setChecked(true);
        }
    }

    private void initialize() {
        setSupportActionBar(mMainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mToolbarText.setText(getResources().getString(R.string.app_name));
    }

    public void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainContainer, fragment, tag);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApplication.getRefWatcher(this);
        refWatcher.watch(this);
    }
}
