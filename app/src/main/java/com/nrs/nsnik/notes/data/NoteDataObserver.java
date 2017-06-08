package com.nrs.nsnik.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import com.nrs.nsnik.notes.interfaces.Observable;
import android.support.v4.content.CursorLoader;
import com.nrs.nsnik.notes.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;

public class NoteDataObserver implements LoaderManager.LoaderCallbacks<Cursor>, Observable{

    private List<Observer> mObserverList;
    private Context mContext;
    private static final int LOADER_ID = 5894;

    public NoteDataObserver(Context context, LoaderManager loaderManager){
        mContext = context;
        mObserverList = new ArrayList<>();
        loaderManager.initLoader(LOADER_ID,null,this);
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
        for(Observer observer:mObserverList){
            observer.updateItems(cursor);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case LOADER_ID:
                return new CursorLoader(mContext, TableNames.mContentUri, null, null, null, null);
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
