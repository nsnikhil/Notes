package com.nrs.nsnik.notes.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.NoteObserverAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.NotesCount;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class NotesFragment extends Fragment implements NotesCount {

    private static final String TAG = NotesFragment.class.getSimpleName();
    @BindView(R.id.commonList)
    RecyclerView mNotesList;
    private Unbinder mUnbinder;
    private String mFolderName = "nofolder";

    public NotesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        return v;
    }

    private void setFolderName() {
        if (getArguments() != null) {
            mFolderName =  getArguments().getString(getActivity().getResources().getString(R.string.foldernamebundle));
        }
    }

    private void initialize() {
        setFolderName();
        mNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        NoteObserverAdapter adapter = new NoteObserverAdapter(getActivity(), TableNames.mContentUri, getLoaderManager(), this,mFolderName);
        mNotesList.setAdapter(adapter);
    }

    private void listeners() {

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
        Log.d(TAG, String.valueOf(count));
    }
}
