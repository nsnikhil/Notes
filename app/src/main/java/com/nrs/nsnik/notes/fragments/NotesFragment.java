package com.nrs.nsnik.notes.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.NoteObserverAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.NotesCount;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class NotesFragment extends Fragment implements NotesCount{

    @BindView(R.id.commonList) RecyclerView mNotesList;
    @BindView(R.id.commonListEmpty)ImageView mEmpty;
    @BindView(R.id.commonAdd)FloatingActionButton mAdd;
    private Unbinder mUnbinder;
    private MenuItem mDeleteMenu;
    private NoteObserverAdapter mNoteAdapter;
    private static final String TAG = NotesFragment.class.getSimpleName();
    Uri mUri;

    public NotesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list,container,false);
        mUnbinder  = ButterKnife.bind(this,v);
        initialize();
        listeners();
        setHasOptionsMenu(true);
        return v;
    }

    private void initialize(){
        if(getArguments()!=null){
            String folderName = getArguments().getString(getActivity().getResources().getString(R.string.foldernamebundle));
            if(folderName!=null){
                mUri = Uri.withAppendedPath(TableNames.mContentUri,folderName);
            }else {
                mUri = TableNames.mContentUri;
            }
        }else {
            mUri  = TableNames.mContentUri;
        }
        mNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mNoteAdapter = new NoteObserverAdapter(getActivity(), mUri, getLoaderManager(),this);
        mNotesList.setAdapter(mNoteAdapter);
        mEmpty.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.emptynotes));
    }

    private void listeners(){
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newNote = new Intent(getActivity(), NewNoteActivity.class);
                if(getArguments()!=null){
                    Log.d(TAG, getArguments().getString(getActivity().getResources().getString(R.string.foldernamebundle)));
                    newNote.putExtra(getActivity().getResources().getString(R.string.newnotefolderbundle)
                            ,getArguments().getString(getActivity().getResources().getString(R.string.foldernamebundle)));
                }startActivity(newNote);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        mDeleteMenu = menu.getItem(1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMainDeleteAll:
                deleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteDialog(){
        AlertDialog.Builder delete = new AlertDialog.Builder(getActivity());
        delete.setTitle(getActivity().getResources().getString(R.string.warning))
                .setMessage(getActivity().getResources().getString(R.string.deleteallnotesfragmentDailog))
                .setNegativeButton(getActivity().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNotes();
            }
        });
        delete.create().show();
    }

    private void deleteNotes(){
        if(getArguments()!=null){
            String folderName = getArguments().getString(getActivity().getResources().getString(R.string.foldernamebundle));
            getActivity().getContentResolver().delete(Uri.withAppendedPath(TableNames.mContentUri,folderName),null,null);
        } else {
            getActivity().getContentResolver().delete(TableNames.mContentUri,null,null);
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

    @Override
    public void getNotesCount(int count) {
        if(count==0){
            if(mDeleteMenu!=null) mDeleteMenu.setVisible(false);
            mEmpty.setVisibility(View.VISIBLE);
        }else {
            if(mDeleteMenu!=null) mDeleteMenu.setVisible(true);
            mEmpty.setVisibility(View.GONE);
        }
    }
}
