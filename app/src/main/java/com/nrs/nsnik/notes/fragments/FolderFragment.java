package com.nrs.nsnik.notes.fragments;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.FolderObserverAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.FolderCount;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FolderFragment extends Fragment implements FolderCount{

    @BindView(R.id.commonList) RecyclerView mFolderList;
    private Uri mUri;
    private int mFolderCount;
    private static final String TAG = FolderFragment.class.getSimpleName();
    private Unbinder mUnbinder;

    public FolderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list,container,false);
        mUnbinder = ButterKnife.bind(this,v);
        initialize();
        listeners();
        return v;
    }

    private void setUri(){
        if(getArguments()!=null){
            String folderName = getArguments().getString(getActivity().getResources().getString(R.string.sunFldName));
            if(folderName!=null){
                mUri = Uri.withAppendedPath(TableNames.mFolderContentUri,folderName);
            }else {
                mUri = TableNames.mFolderContentUri;
            }
        }else {
            mUri = TableNames.mFolderContentUri;
        }
    }

    private void initialize(){
        setUri();
        Log.d(TAG, mUri.toString());
        mFolderList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        FolderObserverAdapter adapter = new FolderObserverAdapter(getActivity(),mUri, getLoaderManager(), this);
        mFolderList.setAdapter(adapter);
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

    protected int getFolderCount() {
        return mFolderCount;
    }

    @Override
    public void getFolderCount(int count) {
        mFolderCount = count;
    }

}
