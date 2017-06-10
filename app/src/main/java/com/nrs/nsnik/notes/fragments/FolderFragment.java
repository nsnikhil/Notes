package com.nrs.nsnik.notes.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.FolderObserverAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.FolderCount;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FolderFragment extends Fragment implements FolderCount {

    private static final String TAG = FolderFragment.class.getSimpleName();
    @BindView(R.id.commonList)
    RecyclerView mFolderList;
    private Uri mUri;
    private int mFolderCount;
    private Unbinder mUnbinder;

    public FolderFragment() {

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
            String folderName = getArguments().getString(getActivity().getResources().getString(R.string.sunFldName));
            if (folderName != null) {
                mUri = Uri.withAppendedPath(TableNames.mFolderContentUri, folderName);
            } else {
                mUri = TableNames.mFolderContentUri;
            }
        } else {
            mUri = TableNames.mFolderContentUri;
        }
    }

    private void initialize() {
        setUri();
        mFolderList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        FolderObserverAdapter adapter = new FolderObserverAdapter(getActivity(), mUri, getLoaderManager(), this);
        mFolderList.setAdapter(adapter);
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
    public void getFolderCount(int count) {
        mFolderCount = count;
    }

}
