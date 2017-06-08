package com.nrs.nsnik.notes.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.FolderObserverAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FolderFragment extends Fragment{

    @BindView(R.id.folderList) RecyclerView mFolderList;
    private FolderObserverAdapter mFolderAdapter;
    private Unbinder mUnbinder;

    public FolderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder,container,false);
        mUnbinder = ButterKnife.bind(this,v);
        initialize();
        return v;
    }

    private void initialize(){
        mFolderList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mFolderAdapter = new FolderObserverAdapter(getActivity(), getLoaderManager());
        mFolderList.setAdapter(mFolderAdapter);
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
