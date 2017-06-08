package com.nrs.nsnik.notes.fragments;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.FolderObserverAdapter;
import com.nrs.nsnik.notes.adapters.NoteObserverAdapter;
import com.nrs.nsnik.notes.data.TableNames;

import java.io.File;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeFragment extends Fragment {

    @BindView(R.id.homeNotesList) RecyclerView mNotesList;
    @BindView(R.id.homeFolderList) RecyclerView mFolderList;
    @BindView(R.id.homeAdd) FloatingActionMenu mAdd;
    private FolderObserverAdapter mFolderAdapter;
    private NoteObserverAdapter mNoteAdapter;
    private Unbinder mUnbinder;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMainDeleteAll:
                deleteAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {
        AlertDialog.Builder delete = new AlertDialog.Builder(getActivity());
        delete.setTitle(getActivity().getResources().getString(R.string.warning))
                .setMessage(getActivity().getResources().getString(R.string.deletealldialog))
                .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDatabase();
            }
        });
        delete.create().show();
    }

    private void clearDatabase(){
        getActivity().getContentResolver().delete(TableNames.mContentUri, null, null);
        getActivity().getContentResolver().delete(TableNames.mFolderContentUri, null, null);
        deleteAllFiles();
    }

    private void deleteAllFiles() {
        File folder = new File(String.valueOf(getActivity().getExternalFilesDir(getResources().getString(R.string.folderName))));
        String child[] = folder.list();
        if (folder.isDirectory()) {
            for (String s : child) {
                new File(folder, s).delete();
            }
        }
    }

    private void initialize() {
        mNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mFolderList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mNoteAdapter = new NoteObserverAdapter(getActivity(), getLoaderManager());
        mFolderAdapter = new FolderObserverAdapter(getActivity(), getLoaderManager());
        mNotesList.setAdapter(mNoteAdapter);
        mFolderList.setAdapter(mFolderAdapter);
        setupNoteFab();
        setFolderFab();
    }

    private void listeners() {

    }

    private void setupNoteFab() {
        final com.github.clans.fab.FloatingActionButton newFile = new com.github.clans.fab.FloatingActionButton(getActivity());
        newFile.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        newFile.setLabelText("New Note");
        newFile.setImageResource(R.drawable.newfilesmall);
        newFile.setColorNormal(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        newFile.setColorPressed(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mAdd.addMenuButton(newFile);
        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdd.close(true);
                Intent newNote = new Intent(getActivity(), NewNoteActivity.class);
                startActivity(newNote);
            }
        });

    }

    private void setFolderFab() {
        com.github.clans.fab.FloatingActionButton newFolder = new com.github.clans.fab.FloatingActionButton(getActivity());
        newFolder.setButtonSize(FloatingActionButton.SIZE_NORMAL);
        newFolder.setLabelText("New Folder");
        newFolder.setImageResource(R.drawable.newfoldersmall);
        newFolder.setColorNormal(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        newFolder.setColorPressed(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        mAdd.addMenuButton(newFolder);
        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdd.close(true);
                AlertDialog.Builder newFolder = new AlertDialog.Builder(getActivity());
                newFolder.setTitle(getResources().getString(R.string.folder));
                final View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_folder_dialog, null);
                newFolder.setView(v);
                newFolder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                newFolder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TextInputEditText editText = (TextInputEditText) v.findViewById(R.id.dialogFolderName);
                        createFolder(editText.getText().toString());
                    }
                });
                newFolder.create().show();
            }
        });
    }

    private void createFolder(String name) {
        if (name != null) {
            ContentValues cv = new ContentValues();
            cv.put(TableNames.table2.mFolderName, name);
            Calendar c = Calendar.getInstance();
            cv.put(TableNames.table2.mFolderId, c.getTimeInMillis() + name);
            Uri u = getActivity().getContentResolver().insert(TableNames.mFolderContentUri, cv);
            if (u != null) {
                Toast.makeText(getActivity(), getResources().getString(R.string.newfoldercreated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.newfoldernotcreated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cleanUp() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        cleanUp();
        super.onDestroy();
    }
}
