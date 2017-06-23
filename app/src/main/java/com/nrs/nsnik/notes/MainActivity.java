package com.nrs.nsnik.notes;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.fragments.HomeFragment;
import com.squareup.haha.perflib.Main;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    private static final String[] mFragTags = {"home", "starred", "recent"};
    @BindView(R.id.mainToolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.mainDrawerLayout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.mainNaviagtionView)
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTheme(R.style.transparentStatusBar);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
        initializeDrawer();
        getSupportFragmentManager().beginTransaction().add(R.id.mainContainer, new HomeFragment(), mFragTags[0]).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menuMainSearch).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMainDeleteAll:
                if (isNotEmpty()) {
                    deleteAll();
                } else {
                    Toast.makeText(MainActivity.this, "Nothing to delete", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNotEmpty() {
        return getContentResolver().query(TableNames.mContentUri, null, null, null, null).getCount() != 0 ||
                getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null).getCount() != 0;
    }

    private void deleteAll() {
        AlertDialog.Builder delete = new AlertDialog.Builder(MainActivity.this);
        delete.setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.deletealldialog))
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDatabase();
            }
        });
        delete.create().show();
    }

    private void clearDatabase() {
        getContentResolver().delete(TableNames.mFolderContentUri, null, null);
        getContentResolver().delete(TableNames.mContentUri, null, null);
        deleteAllFiles();
    }

    private void deleteAllFiles() {
        File folder = new File(String.valueOf(MainActivity.this.getExternalFilesDir(getResources().getString(R.string.folderName))));
        String child[] = folder.list();
        if (folder.isDirectory()) {
            for (String s : child) {
                new File(folder, s).delete();
            }
        }
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
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
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
