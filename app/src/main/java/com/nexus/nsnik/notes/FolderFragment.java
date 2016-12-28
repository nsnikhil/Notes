package com.nexus.nsnik.notes;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.nexus.nsnik.notes.data.TableNames;

public class FolderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    GridView foldersList;
    private static final int mFolderLoaderId = 5784;
    FolderAdapter folderAdapter;

    public FolderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder,container,false);
        initilize(v);
        folderAdapter = new FolderAdapter(getActivity(),null);
        foldersList.setAdapter(folderAdapter);
        loadFolder();
        return v;
    }

    private void initilize(View v) {
        foldersList = (GridView)v.findViewById(R.id.folderFragmentGridView);
        foldersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent folderContent = new Intent(getActivity(),MainActivity.class);
                TextView tv = (TextView) view.findViewById(R.id.singleFolderName);
                folderContent.putExtra(getResources().getString(R.string.foldernamebundle),tv.getText().toString());
                folderContent.setData(Uri.withAppendedPath(TableNames.mFolderContentUri, tv.getText().toString()));
                startActivity(folderContent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case mFolderLoaderId:
               return new CursorLoader(getActivity(), TableNames.mFolderContentUri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        folderAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        folderAdapter.swapCursor(null);
    }

    private void loadFolder() {
        if (getActivity().getSupportLoaderManager().getLoader(mFolderLoaderId) == null) {
            getActivity().getSupportLoaderManager().initLoader(mFolderLoaderId, null, this).forceLoad();
        } else {
            getActivity().getSupportLoaderManager().restartLoader(mFolderLoaderId, null, this).forceLoad();
        }
    }
}
