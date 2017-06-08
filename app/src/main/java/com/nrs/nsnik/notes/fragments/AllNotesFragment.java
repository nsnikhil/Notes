package com.nrs.nsnik.notes.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.NoteObserverAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AllNotesFragment extends Fragment{

    @BindView(R.id.notesList) RecyclerView mNotesList;
    private Unbinder mUnbinder;
    private NoteObserverAdapter mNoteAdapter;

    public AllNotesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_notes,container,false);
        mUnbinder  = ButterKnife.bind(this,v);
        initialize();
        return v;
    }

    private void initialize(){
        mNotesList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mNoteAdapter = new NoteObserverAdapter(getActivity(), getLoaderManager());
        mNotesList.setAdapter(mNoteAdapter);
    }

    private void listeners(){

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
