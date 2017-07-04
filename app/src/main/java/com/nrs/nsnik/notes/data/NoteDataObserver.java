package com.nrs.nsnik.notes.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.nrs.nsnik.notes.interfaces.Observable;
import com.nrs.nsnik.notes.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;

/*
this is an observable i.e. observers
can observe on this class
and they will be notified once the
observable emits new set of data
it uses cursor loader to watch out on a uri
passed and whenever somethings changes in that uri
it send the new data to all of its observers
 */


public class NoteDataObserver implements LoaderManager.LoaderCallbacks<Cursor>, Observable {

    private static final int LOADER_ID = 1;
    private static final String TAG = NoteDataObserver.class.getSimpleName();
    private List<Observer> mObserverList;
    private Context mContext;
    private Uri mUri;

    /*
     @param uri              the uri to query on
     @param loaderManager    loader manager object
    */
    public NoteDataObserver(Context context, Uri uri, LoaderManager loaderManager) {
        mContext = context;
        mObserverList = new ArrayList<>();
        mUri = uri;
        loaderManager.initLoader(LOADER_ID, null, this);
    }

    /*
     @param observers    the observers which are interested on data emitted
                         by this observable and want ot be notified every time
                         data changes
    */
    @Override
    public void add(Observer observer) {
        mObserverList.add(observer);
    }

    @Override
    public void remove(Observer observer) {
    }

    /*
    @param cursor   all its observers those have registered to listen for change are
                    send a new cursor on data changed
     */
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
