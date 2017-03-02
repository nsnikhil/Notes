package com.nrs.nsnik.notes;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.nrs.nsnik.notes.data.TableNames;

import java.io.File;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    GridView mainFragmentGridView;
    NoteAdapter noteAdapter = null;
    private static final int mLoaderId = 20154;
    String folderUri = null;
    Uri mainUri = null;

    public MainFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main,container,false);
        initilize(v);
        if(getActivity().getIntent().getData()!=null){
            folderUri = getActivity().getIntent().getExtras().getString(getResources().getString(R.string.foldernamebundle));
            mainUri = Uri.withAppendedPath(TableNames.mContentUri,folderUri);
        }else {
            mainUri = Uri.withAppendedPath(TableNames.mContentUri,getResources().getString(R.string.nofolder));
        }
        noteAdapter = new NoteAdapter(getActivity(),null);
        mainFragmentGridView.setAdapter(noteAdapter);
        loadNote();
        return v;
    }

    private void initilize(View v) {
        mainFragmentGridView = (GridView) v.findViewById(R.id.mainFragmentGridView);
        mainFragmentGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent editor = new Intent(getActivity(),NewNoteActivity.class);
                if(getActivity().getIntent().getData()!=null){
                    editor.setData(Uri.withAppendedPath(TableNames.mContentUri,folderUri));
                }else {
                    editor.setData(Uri.withAppendedPath(TableNames.mContentUri,getResources().getString(R.string.nofolder)));
                }
                Log.d("Uri",editor.getData().toString());
                startActivity(editor);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case mLoaderId:
                    return new CursorLoader(getActivity(), mainUri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        noteAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        noteAdapter.swapCursor(null);
    }

    private void loadNote() {
        if (getActivity().getSupportLoaderManager().getLoader(mLoaderId) == null) {
            getActivity().getSupportLoaderManager().initLoader(mLoaderId, null, this).forceLoad();
        } else {
            getActivity().getSupportLoaderManager().restartLoader(mLoaderId, null, this).forceLoad();
        }
    }
}
