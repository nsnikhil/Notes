/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.dagger.qualifiers.ApplicationQualifier;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.NoteObject;

import java.util.Calendar;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class DatabaseOperations {

    private final Context mContext;
    private AsyncQueryHandler mAsyncQueryHandler;

    @Inject
    DatabaseOperations(@ApplicationQualifier Context context) {
        mContext = context;
        initialize();
    }


    @SuppressLint("HandlerLeak")
    private void initialize() {
        mAsyncQueryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {
            @Override
            protected Handler createHandler(Looper looper) {
                return super.createHandler(looper);
            }

            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                if (uri == null) {
                    Timber.d(mContext.getResources().getString(R.string.dbMessageInsertFailed));
                } else {
                    Timber.d(mContext.getResources().getString(R.string.dbMessageInsertSuccessful));
                }
                super.onInsertComplete(token, cookie, uri);
            }

            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {
                if (result == 0) {
                    Timber.d(mContext.getResources().getString(R.string.dbMessageUpdateFailed));
                } else {
                    Timber.d(mContext.getResources().getString(R.string.dbMessageUpdateSuccessful));
                }
                super.onUpdateComplete(token, cookie, result);
            }

            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                if (result == 0) {
                    Timber.d(mContext.getResources().getString(R.string.dbMessageDeleteFailed));
                } else {
                    Timber.d(mContext.getResources().getString(R.string.dbMessageDeleteSuccessful));
                }
                super.onDeleteComplete(token, cookie, result);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                Timber.d(String.valueOf(cursor.getCount()));
                super.onQueryComplete(token, cookie, cursor);
            }
        };
    }


    public void insertFolder(Uri uri, String folderName, String parentFolderName, String folderColor) {
        ContentValues cv = new ContentValues();
        cv.put(TableNames.table2.mFolderName, folderName);
        Calendar c = Calendar.getInstance();
        cv.put(TableNames.table2.mFolderId, c.getTimeInMillis() + folderName);
        cv.put(TableNames.table2.mParentFolderName, parentFolderName);
        cv.put(TableNames.table2.mColor, folderColor);
        mAsyncQueryHandler.startInsert(1, null, uri, cv);
    }


    void updateFolder(Uri uri, String folderName, String parentFolderName, String folderColor, String selection, String[] selectionArgs) {
        ContentValues cv = new ContentValues();
        cv.put(TableNames.table2.mFolderName, folderName);
        cv.put(TableNames.table2.mParentFolderName, parentFolderName);
        cv.put(TableNames.table2.mColor, folderColor);
        mAsyncQueryHandler.startUpdate(1, null, uri, cv, selection, selectionArgs);

        //Change the uri to match the uri which loads the current set of list
        mContext.getContentResolver().notifyChange(TableNames.mFolderContentUri, null);
    }


    /*
    @param fileName     the name of the fie which contains the note object
    @param noteObject   the note object that represents a single note
     */
    void insertNote(String fileName, NoteObject noteObject, int isPinned, int isLocked, String time, String color) {
        ContentValues cv = new ContentValues();
        cv.put(TableNames.table1.mTitle, noteObject.getTitle());
        cv.put(TableNames.table1.mFileName, fileName);
        cv.put(TableNames.table1.mFolderName, noteObject.getFolderName());
        cv.put(TableNames.table1.mIsPinned, isPinned);
        cv.put(TableNames.table1.mIsLocked, isLocked);
        cv.put(TableNames.table1.mDataModified, time);
        cv.put(TableNames.table1.mColor, color);
        mAsyncQueryHandler.startInsert(1, null, TableNames.mContentUri, cv);
    }

    /*
   @param title    the title of the note to be updated
   @param uri      the uri on which update operation will be performed
    */
    void updateNote(String title, Uri uri, int isPinned, int isLocked, String time, String color) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableNames.table1.mTitle, title);

        contentValues.put(TableNames.table1.mIsPinned, isPinned);
        contentValues.put(TableNames.table1.mIsLocked, isLocked);
        contentValues.put(TableNames.table1.mDataModified, time);
        contentValues.put(TableNames.table1.mColor, color);

        mAsyncQueryHandler.startUpdate(1, null, uri, contentValues, null, null);

        //Change the uri to match the uri which loads the current set of list
        mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);
    }

    void queryNote(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrderBy) {
        mAsyncQueryHandler.startQuery(1, null, uri, projection, selection, selectionArgs, sortOrderBy);
    }


    /*
    @param uri    uri of the path in database to be deleted
     */
    void deleteNote(Uri uri) {
        mAsyncQueryHandler.startDelete(0, null, uri, null, null);

        //Change the uri to match the uri which loads the current set of list
        mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);
    }

    /*
    @param uri              uri of the path in database to be deleted
    @param folderName       the name of the folder to be deleted
     */
    void deleteFolder(Uri uri, String folderName) {
        String query = "parentFolderName/" + folderName;
        mAsyncQueryHandler.startDelete(0, null, Uri.withAppendedPath(TableNames.mContentUri, query), null, null);
        mAsyncQueryHandler.startDelete(0, null, uri, null, null);

        //Change the uri to match the uri which loads the current set of list
        mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);
        mContext.getContentResolver().notifyChange(TableNames.mFolderContentUri, null);
    }


    /*
    This method takes id of two notes and swaps them by
    first assigning a temporary id to them and then finally swapping
    the temporary with the actual

    @param fromId       the id of the first note
    @param toId         the id of second note
     */
    public void switchNoteId(int fromId, int toId) {
        if (fromId != -1 && toId != -1) {
            int tempFromId = generateRandom(9999, 5000), tempToID = generateRandom(4999, 1000);

            //Change actual to temp


            Uri fromUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + fromId);
            Uri toUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + toId);

            ContentValues fromContentValues = new ContentValues();
            fromContentValues.put(TableNames.table1.mUid, tempFromId);

            ContentValues toContentValues = new ContentValues();
            toContentValues.put(TableNames.table1.mUid, tempToID);

            mContext.getContentResolver().update(fromUri, fromContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);
            mContext.getContentResolver().update(toUri, toContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);


            //Change temp To Actual

            Uri newFomUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + tempFromId);
            Uri newToUri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + tempToID);

            ContentValues newFromContentValues = new ContentValues();
            newFromContentValues.put(TableNames.table1.mUid, toId);

            ContentValues newToContentValues = new ContentValues();
            newToContentValues.put(TableNames.table1.mUid, fromId);

            mContext.getContentResolver().update(newFomUri, newFromContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);
            mContext.getContentResolver().update(newToUri, newToContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mContentUri, null);
        } else {
            Timber.d("Database Error");
        }
    }

    /*
    This method takes id of two folders and swaps them by
    first assigning a temporary id to them and then finally swapping
    the temporary with the actual

    @param fromId       the id of the first folder
    @param toId         the id of second folder
     */
    public void switchFolderId(int fromId, int toId) {

        if (fromId != -1 && toId != -1) {

            int tempFromId = generateRandom(9999, 5000), tempToID = generateRandom(4999, 1000);

            //Change actual to temp

            Uri fromUri = Uri.withAppendedPath(TableNames.mFolderContentUri, "folderId/" + fromId);
            Uri toUri = Uri.withAppendedPath(TableNames.mFolderContentUri, "folderId/" + toId);

            //8336085805
            ContentValues fromContentValues = new ContentValues();
            fromContentValues.put(TableNames.table2.mUid, tempFromId);

            ContentValues toContentValues = new ContentValues();
            toContentValues.put(TableNames.table2.mUid, tempToID);


            mContext.getContentResolver().update(fromUri, fromContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mFolderContentUri, null);
            mContext.getContentResolver().update(toUri, toContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mFolderContentUri, null);


            //Change temp To Actual

            Uri newFomUri = Uri.withAppendedPath(TableNames.mFolderContentUri, "folderId/" + tempFromId);
            Uri newToUri = Uri.withAppendedPath(TableNames.mFolderContentUri, "folderId/" + tempToID);

            ContentValues newFromContentValues = new ContentValues();
            newFromContentValues.put(TableNames.table2.mUid, toId);

            ContentValues newToContentValues = new ContentValues();
            newToContentValues.put(TableNames.table2.mUid, fromId);


            mContext.getContentResolver().update(newFomUri, newFromContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mFolderContentUri, null);
            mContext.getContentResolver().update(newToUri, newToContentValues, null, null);
            mContext.getContentResolver().notifyChange(TableNames.mFolderContentUri, null);
        } else {
            Timber.d("Database Error");
        }
    }


    /*
    this method takes a uri and
    provides the id of a note or folder in
    particular position

    @param uri          the uri which will be searched
    @param position     the position at which the search will end
   */
    public int getId(Uri uri, int position) {
        int uid = -1;
        Cursor tempCursor = mContext.getContentResolver().query(uri, null, null, null, null);
        try {
            if (tempCursor != null) {
                for (int i = 0; i <= tempCursor.getCount(); i++) {
                    if (i <= position && tempCursor.moveToNext()) {
                        uid = tempCursor.getInt(tempCursor.getColumnIndex(TableNames.table1.mUid));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tempCursor != null) {
                tempCursor.close();
            }
        }
        return uid;
    }


    /*
    takes max and min and finds a random no
    between that range

    @param max      the maximum range value
    @param min      the minimum range value
     */
    private int generateRandom(int max, int min) {
        return new Random().nextInt((max - min) + 1) + min;
    }

}
