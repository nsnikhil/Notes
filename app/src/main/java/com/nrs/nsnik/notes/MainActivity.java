package com.nrs.nsnik.notes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.nrs.nsnik.notes.fragments.NotesFragment;
import com.nrs.nsnik.notes.fragments.FolderFragment;
import com.nrs.nsnik.notes.fragments.HomeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainToolbar) Toolbar mMainToolbar;
    @BindView(R.id.mainDrawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.mainNaviagtionView) NavigationView mNavigationView;
    private static final String[] mFragTags = {"home","notes","folder"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
        initializeDrawer();
        getSupportFragmentManager().beginTransaction().add(R.id.mainContainer, new HomeFragment(),mFragTags[0]).commit();
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
                    case R.id.navigationMyNotes:
                        if(getSupportFragmentManager().findFragmentByTag(mFragTags[0])==null){
                            replaceFragment(new HomeFragment(),mFragTags[0]);
                        }
                        drawerAction(0);
                        break;
                    case R.id.navigationAllNotes:
                        if(getSupportFragmentManager().findFragmentByTag(mFragTags[1])==null){
                            replaceFragment(new NotesFragment(),mFragTags[1]);
                        }
                        drawerAction(1);
                        break;
                    case R.id.navigationFolder:
                        if(getSupportFragmentManager().findFragmentByTag(mFragTags[2])==null){
                            replaceFragment(new FolderFragment(),mFragTags[2]);
                        }
                        drawerAction(2);
                        break;
                    case R.id.navigationSettings:
                        break;
                }
                return true;
            }
        });
    }

    private void drawerAction(int key) {
        MenuItem notes = mNavigationView.getMenu().getItem(0).setChecked(false);
        MenuItem allNotes = mNavigationView.getMenu().getItem(1).setChecked(false);
        MenuItem folder = mNavigationView.getMenu().getItem(2).setChecked(false);
        switch (key) {
            case 0:
                notes.setChecked(true);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                break;
            case 1:
                allNotes.setChecked(true);
                getSupportActionBar().setTitle(getResources().getString(R.string.allnotes));
                break;
            case 2:
                folder.setChecked(true);
                getSupportActionBar().setTitle(getResources().getString(R.string.myfolder));
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

}
