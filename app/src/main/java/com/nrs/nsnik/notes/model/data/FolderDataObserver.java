/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.nrs.nsnik.notes.util.interfaces.NoteObservable;
import com.nrs.nsnik.notes.util.interfaces.NoteObserver;

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

public class FolderDataObserver implements LoaderManager.LoaderCallbacks<Cursor>, NoteObservable {

    private static final int LOADER_ID = 2;
    private final Context mContext;
    private final Uri mUri;
    private final List<NoteObserver> mNoteObserverList;

    /*
    @param uri              the uri to query on
    @param loaderManager    loader manager object
     */
    public FolderDataObserver(Context context, Uri uri, LoaderManager loaderManager) {
        mContext = context;
        mNoteObserverList = new ArrayList<>();
        mUri = uri;
        loaderManager.initLoader(LOADER_ID, null, this);
    }

    /*
    @param observers    the observers which are interested on data emitted
                        by this observable and want ot be notified every time
                        data changes
     */
    @Override
    public void add(NoteObserver noteObserver) {
        mNoteObserverList.add(noteObserver);
    }

    @Override
    public void remove(NoteObserver noteObserver) {
    }

    /*
    @param cursor   all its observers those have registered to listen for change are
                    send a new cursor on data changed
     */
    @Override
    public void updateObserver(Cursor cursor) {
        for (NoteObserver noteObserver : mNoteObserverList) {
            noteObserver.updateItems(cursor);
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
