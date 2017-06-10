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
    private int mNotesCount;
    private Uri mUri;

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

    private void setUri() {
        if (getArguments() != null) {
            String folderName = getArguments().getString(getActivity().getResources().getString(R.string.foldernamebundle));
            if (folderName != null) {
                mUri = Uri.withAppendedPath(TableNames.mContentUri, folderName);
            } else {
                mUri = TableNames.mContentUri;
            }
        } else {
            mUri = TableNames.mContentUri;
        }
    }

    private void initialize() {
        setUri();
        Log.d(TAG, mUri.toString());
        mNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        NoteObserverAdapter adapter = new NoteObserverAdapter(getActivity(), mUri, getLoaderManager(), this);
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
        mNotesCount = count;
    }
}
