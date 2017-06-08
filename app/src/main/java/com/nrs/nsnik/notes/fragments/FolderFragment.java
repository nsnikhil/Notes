package com.nrs.nsnik.notes.fragments;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    @BindView(R.id.commonListEmpty)ImageView mEmpty;
    @BindView(R.id.commonAdd)FloatingActionButton mAdd;
    private FolderObserverAdapter mFolderAdapter;
    private Unbinder mUnbinder;
    private MenuItem mDeleteMenu;

    public FolderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_list,container,false);
        mUnbinder = ButterKnife.bind(this,v);
        initialize();
        listeners();
        setHasOptionsMenu(true);
        return v;
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialize(){
        mFolderList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mFolderAdapter = new FolderObserverAdapter(getActivity(), getLoaderManager(),this);
        mFolderList.setAdapter(mFolderAdapter);
        mEmpty.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.emptyfolder));
    }

    private void listeners(){
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder newFolder = new AlertDialog.Builder(getActivity());
                newFolder.setTitle(getResources().getString(R.string.folder));
                final View view = LayoutInflater.from(getActivity()).inflate(R.layout.new_folder_dialog, null);
                newFolder.setView(view);
                newFolder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                newFolder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText) view.findViewById(R.id.dialogFolderName);
                        createFolder(editText.getText().toString());
                    }
                });
                newFolder.create().show();
            }
        });
    }

    private void createFolder(String name) {
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
        if(count==0){
            if(mDeleteMenu!=null) mDeleteMenu.setVisible(false);
            mEmpty.setVisibility(View.VISIBLE);
        }else {
            if(mDeleteMenu!=null) mDeleteMenu.setVisible(true);
            mEmpty.setVisibility(View.GONE);
        }
    }

}
