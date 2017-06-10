package com.nrs.nsnik.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.nrs.nsnik.notes.interfaces.Observable;
import com.nrs.nsnik.notes.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;


public class FolderDataObserver implements LoaderManager.LoaderCallbacks<Cursor>, Observable {

    private List<Observer> mObserverList;
    private Context mContext;
    private static final int LOADER_ID = 2;
    private Uri mUri;
    private static final String TAG = FolderDataObserver.class.getSimpleName();

    public FolderDataObserver(Context context,Uri uri, LoaderManager loaderManager) {
        mContext = context;
        mObserverList = new ArrayList<>();
        mUri = uri;
        loaderManager.initLoader(LOADER_ID, null, this);
    }


    @Override
    public void add(Observer observer) {
        mObserverList.add(observer);
    }

    @Override
    public void remove(Observer observer) {

    }

    @Override
    public void updateObserver(Cursor cursor) {
        for (Observer observer : mObserverList) {
            observer.updateItems(cursor);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(mContext, mUri, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateObserver(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
