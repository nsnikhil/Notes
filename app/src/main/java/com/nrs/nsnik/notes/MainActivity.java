package com.nrs.nsnik.notes;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.data.TableNames.table2;

import java.io.File;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    Toolbar mainToolbar;
    DrawerLayout mainDrawerLayout;
    NavigationView mainNaviagtionView;
    ActionBarDrawerToggle mainDrawerToggle;
    FloatingActionMenu mainFab;
    String folderName = null;
    EditText name;
    Uri folderUri = null;
    String UrifolderName = null;
    ImageView emptyState;
    Fragment mFoldersFragment = null;
    Fragment mNotesFragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initilize();
        initilizeDrawer();
        setEmpty();
        folderUri = getIntent().getData();
        if (folderUri != null) {
            UrifolderName = getIntent().getExtras().getString(getResources().getString(R.string.foldernamebundle));
            getSupportActionBar().setTitle(UrifolderName.toUpperCase());
            mainDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mainDrawerToggle.setDrawerIndicatorEnabled(false);
            mainDrawerToggle.syncState();
        }
        invalidateOptionsMenu();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            if (folderUri == null) {
                mFoldersFragment = new FolderFragment();
                ft.add(R.id.mainFolderContainer, mFoldersFragment);
            }
            mNotesFragment = new MainFragment();
            ft.add(R.id.mainContainer, mNotesFragment).commit();
        }
        setupFab();
        setFolderFab();
    }

    private void initilizeDrawer() {
        mainDrawerLayout = (DrawerLayout) findViewById(R.id.mainDrawerLayout);
        mainNaviagtionView = (NavigationView) findViewById(R.id.mainNaviagtionView);
        mainDrawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolbar, R.string.drawerOpen, R.string.drawerClose) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mainDrawerLayout.setDrawerListener(mainDrawerToggle);
        mainDrawerToggle.syncState();
        mainNaviagtionView.getMenu().getItem(0).setChecked(true);
        mainNaviagtionView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigationMyNotes:
                        mNotesFragment = new MainFragment();
                        ft.replace(R.id.mainContainer, mNotesFragment);
                        mFoldersFragment = new FolderFragment();
                        ft.replace(R.id.mainFolderContainer, mFoldersFragment);
                        drawerAction(0);
                        break;
                    case R.id.navigationAllNotes:
                        if (mFoldersFragment != null) {
                            ft.remove(mFoldersFragment);
                            mFoldersFragment = null;
                        }
                        mNotesFragment = new AllNotesFragment();
                        ft.replace(R.id.mainContainer, mNotesFragment);
                        drawerAction(1);
                        break;
                    case R.id.navigationFolder:
                        if (mNotesFragment != null) {
                            ft.remove(mNotesFragment);
                            mNotesFragment = null;
                        }
                        mFoldersFragment = new FolderFragment();
                        ft.replace(R.id.mainFolderContainer, mFoldersFragment);
                        drawerAction(2);
                        break;
                    case R.id.navigationSettings:
                        startActivity(new Intent(MainActivity.this, Prefrences.class));
                        break;
                }
                ft.commit();
                return true;
            }
        });
    }

    private void drawerAction(int key) {
        invalidateOptionsMenu();
        MenuItem notes = mainNaviagtionView.getMenu().getItem(0).setChecked(false);
        MenuItem allnotes = mainNaviagtionView.getMenu().getItem(1).setChecked(false);
        MenuItem folder = mainNaviagtionView.getMenu().getItem(2).setChecked(false);
        mainDrawerLayout.closeDrawers();
        mainFab.removeAllMenuButtons();
        setupFab();
        setFolderFab();
        switch (key) {
            case 0:
                notes.setChecked(true);
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                break;
            case 1:
                allnotes.setChecked(true);
                getSupportActionBar().setTitle(getResources().getString(R.string.allnotes));
                break;
            case 2:
                folder.setChecked(true);
                getSupportActionBar().setTitle(getResources().getString(R.string.myfolder));
        }
    }

    private void initilize() {
        mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        mainToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mainFab = (FloatingActionMenu) findViewById(R.id.mainFab);
        emptyState = (ImageView) findViewById(R.id.mainEmptyState);
    }

    private void setEmpty() {
        Cursor c = getContentResolver().query(TableNames.mContentUri, null, null, null, null);
        Cursor fc = getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null);
        if (c.getCount() != 0 || fc.getCount() != 0) {
            emptyState.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.VISIBLE);
        }

    }


    private void setupFab() {
        final com.github.clans.fab.FloatingActionButton newFile = new com.github.clans.fab.FloatingActionButton(getApplicationContext());
        newFile.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        newFile.setLabelText("New Note");
        newFile.setImageResource(R.drawable.newfilesmall);
        newFile.setColorNormal(getResources().getColor(R.color.colorAccent));
        newFile.setColorPressed(getResources().getColor(R.color.colorAccent));
        if (mNotesFragment != null) {
            mainFab.addMenuButton(newFile);
            newFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainFab.close(true);
                    Intent newNote = new Intent(MainActivity.this, NewNoteActivity.class);
                    if (folderUri != null) {
                        newNote.putExtra(getResources().getString(R.string.newnotefolderbundle), UrifolderName);
                    }
                    startActivity(newNote);
                }
            });
        }
    }

    private void setFolderFab() {
        com.github.clans.fab.FloatingActionButton newFolder = new com.github.clans.fab.FloatingActionButton(getApplicationContext());
        newFolder.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        newFolder.setLabelText("New Folder");
        newFolder.setImageResource(R.drawable.newfoldersmall);
        newFolder.setColorNormal(getResources().getColor(R.color.colorAccent));
        newFolder.setColorPressed(getResources().getColor(R.color.colorAccent));
        if (mFoldersFragment != null) {
            mainFab.addMenuButton(newFolder);
            newFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainFab.close(true);
                    AlertDialog.Builder newFolder = new AlertDialog.Builder(MainActivity.this);
                    newFolder.setTitle(getResources().getString(R.string.folder));
                    View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.new_folder_dialog, null);
                    newFolder.setView(v);
                    name = (EditText) v.findViewById(R.id.dialogFolderName);
                    newFolder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    newFolder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            folderName = name.getText().toString();
                            createFolder(folderName);
                            invalidateOptionsMenu();
                        }
                    });
                    newFolder.create().show();
                }
            });
        }
    }

    private void createFolder(String name) {
        if (name != null) {
            ContentValues cv = new ContentValues();
            cv.put(table2.mFolderName, name);
            Calendar c = Calendar.getInstance();
            cv.put(table2.mFolderId, c.getTimeInMillis() + name);
            Uri u = getContentResolver().insert(TableNames.mFolderContentUri, cv);
            if (u != null) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.newfoldercreated), Toast.LENGTH_SHORT).show();
                setEmpty();
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.newfoldernotcreated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        setEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem serachItem = menu.findItem(R.id.menuMainSearch);
        android.widget.SearchView searchView = (android.widget.SearchView) MenuItemCompat.getActionView(serachItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchResultActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteall = menu.findItem(R.id.menuMainDeleteAll);
        if (getContentResolver().query(TableNames.mContentUri, null, null, null, null).getCount() == 0 && getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null).getCount()==0) {
            deleteall.setVisible(false);
        }else if(mFoldersFragment==null&&getContentResolver().query(TableNames.mContentUri, null, null, null, null).getCount() == 0 ){
            deleteall.setVisible(false);
        }else if(mNotesFragment==null&&getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null).getCount() == 0){
            deleteall.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMainDeleteAll:
                if (mFoldersFragment != null && mNotesFragment != null) {
                    if (folderUri == null && UrifolderName == null) {
                        if (getContentResolver().query(TableNames.mContentUri, null, null, null, null).getCount() == 0 && getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null).getCount() == 0) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletenoitem), Toast.LENGTH_SHORT).show();
                        } else {
                            showAlertDialog(TableNames.mContentUri, 0);
                        }
                    } else {
                        if (getContentResolver().query(Uri.withAppendedPath(TableNames.mContentUri, UrifolderName), null, null, null, null).getCount() == 0) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletenoitem), Toast.LENGTH_SHORT).show();
                        } else {
                            showAlertDialog(Uri.withAppendedPath(TableNames.mContentUri, UrifolderName), 1);
                        }
                    }
                } else if (mFoldersFragment == null && mNotesFragment != null) {
                    fragmentDialog(1);
                } else if (mNotesFragment == null && mFoldersFragment != null) {
                    fragmentDialog(0);
                }
                break;
            case R.id.menuMainSearch:

                break;
        }
        return true;
    }




    private void fragmentDialog(final int key) {
        AlertDialog.Builder del = new AlertDialog.Builder(MainActivity.this);
        del.setTitle(getResources().getString(R.string.warning));
        if (key == 0) {
            del.setMessage(getResources().getString(R.string.deleteallfolderDailog));
        } else {
            del.setMessage(getResources().getString(R.string.deleteallnotesfragmentDailog));
        }
        del.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        del.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                long count;
                if (key == 0) {
                    Cursor c = getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null);
                    while (c.moveToNext()) {
                        String nm = c.getString(c.getColumnIndex(table2.mFolderName));
                        deleteNotesInFolder(nm);
                    }
                    count = getContentResolver().delete(TableNames.mFolderContentUri, null, null);
                    if (count != 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteallfolder), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteallfolderfailed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    deleteAllNotes();
                    count = getContentResolver().delete(TableNames.mContentUri, null, null);
                    if (count != 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletedallnotes), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteallnotesfailed), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        del.create().show();
    }


    private void deleteAllNotes() {
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        String child[] = folder.list();
        if (folder.isDirectory()) {
            for (String s : child) {
                new File(folder, s).delete();
            }
        }
    }

    private void deleteNotesInFolder(String fldrNm) {
        Cursor c = getContentResolver().query(Uri.withAppendedPath(TableNames.mContentUri, fldrNm), null, null, null, null);
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        while (c.moveToNext()) {
            String filename = c.getString(c.getColumnIndex(TableNames.table1.mFileName));
            File f = new File(folder, filename);
            if (f.exists()) {
                f.delete();
            }
        }
        long count = getContentResolver().delete(Uri.withAppendedPath(TableNames.mContentUri, fldrNm), null, null);
    }


    private boolean checkFirst() {
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int count = spf.getInt(getResources().getString(R.string.count), 0);
        if (count == 1) {
            return false;
        } else {
            return true;
        }
    }

    private void showAlertDialog(final Uri d, final int key) {
        AlertDialog.Builder delete = new AlertDialog.Builder(MainActivity.this);
        delete.setTitle(getResources().getString(R.string.warning));
        if (key == 0) {
            delete.setMessage(getResources().getString(R.string.deletealldialog));
        } else {
            delete.setMessage(getResources().getString(R.string.deleteallnotesdialog));
        }
        delete.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        delete.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (key == 0) {
                    int count = 0;
                    int count2 = 0;
                    if (getContentResolver().query(TableNames.mContentUri, null, null, null, null).getCount() > 0) {
                        count = getContentResolver().delete(d, null, null);
                        invalidateOptionsMenu();
                        setEmpty();
                    }
                    if (getContentResolver().query(TableNames.mFolderContentUri, null, null, null, null).getCount() > 0) {
                        count2 = getContentResolver().delete(TableNames.mFolderContentUri, null, null);
                        invalidateOptionsMenu();
                        setEmpty();
                    }
                    if (count != 0) {
                        deleteAll(count);
                    } else if (count2 != 0) {
                        deleteAll(count2);
                    }
                } else {
                    deleteFolderNotes();
                    int count = getContentResolver().delete(d, null, null);
                    if (count == 0) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteallnotesfailed), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletedallnotes), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        delete.create().show();
    }


    private void deleteAll(int c) {
        if (c == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteallfailed), Toast.LENGTH_SHORT).show();
        } else {
            File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
            String child[] = folder.list();
            if (folder.isDirectory()) {
                for (String s : child) {
                    new File(folder, s).delete();
                }
            }
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.deletedall), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFolderNotes() {
        Cursor c = getContentResolver().query(Uri.withAppendedPath(TableNames.mContentUri, UrifolderName), null, null, null, null);
        File folder = new File(String.valueOf(getExternalFilesDir(getResources().getString(R.string.folderName))));
        try {
            if (c.moveToNext()) {
                String name = c.getString(c.getColumnIndex(TableNames.table1.mFileName));
                File f = new File(folder, name);
                if (f.exists()) {
                    f.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
