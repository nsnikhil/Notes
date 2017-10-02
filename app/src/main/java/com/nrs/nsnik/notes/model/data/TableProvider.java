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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nrs.nsnik.notes.model.data.TableNames.table1;
import com.nrs.nsnik.notes.model.data.TableNames.table2;
import com.nrs.nsnik.notes.view.MyApplication;


public class TableProvider extends ContentProvider {

    /**
     * uAllNotes return all notes
     * uSingleNoteByNoteId return a note of specific id
     * uAllNoteByParentFolderName return notes with a specific parent name supplied as arguments
     * uAllNoteBySearchQuery return notes whose title begin with the argument supplied
     * uAllNotesThatArePinned return all notes that are pinned
     * uAllNotesThatAreLocked return all notes that are locked
     * uAllNotesByColor return note of specific color supplied as argument
     * uAllFolder return all folders
     * uSingleFolderByFolderId return folder of a specific id
     * uSingleFolderByName return folder with a specific name supplied as argument
     * uAllFolderByParentName return folders with a specific parent name supplied as argument
     * uAllFolderBySearchQuery return folders whose name begin with the argument supplied
     * uAllFoldersThatArePinned return all folders that are pinned
     * uAllFoldersThatAreLocked return all folder that are locked
     * uAllFoldersByColor return folder by a specific color supplied as argument
     */
    private static final int uAllNotes = 111;
    private static final int uSingleNoteByNoteId = 112;
    private static final int uAllNoteByParentFolderName = 113;
    private static final int uAllNoteBySearchQuery = 114;
    private static final int uAllNotesThatArePinned = 115;
    private static final int uAllNotesThatAreLocked = 116;
    private static final int uAllNotesByColor = 117;


    private static final int uAllFolder = 211;
    private static final int uSingleFolderByFolderId = 212;
    private static final int uSingleFolderByName = 213;
    private static final int uAllFolderByParentName = 214;
    private static final int uAllFolderBySearchQuery = 215;
    private static final int uAllFoldersThatArePinned = 216;
    private static final int uAllFoldersThatAreLocked = 217;
    private static final int uAllFoldersByColor = 218;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /*
     * Adding list of uri to UriMatcher
     * 1st Param  - Base Authority
     * 2nd Param  - Path to a specific uri
     * 3rd param  - integer constant which map to a uri
     */
    static {
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/noteId/#", uSingleNoteByNoteId);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/parentFolderName/*", uAllNoteByParentFolderName);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/pinned/#", uAllNotesThatArePinned);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/locked/#", uAllNotesThatAreLocked);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/color/*", uAllNotesByColor);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/search/*", uAllNoteBySearchQuery);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName, uAllNotes);

        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/folderId/#", uSingleFolderByFolderId);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/folderName/*", uSingleFolderByName);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/parentFolderName/*", uAllFolderByParentName);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/pinned/#", uAllFoldersThatArePinned);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/locked/#", uAllFoldersThatAreLocked);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/color/*", uAllFoldersByColor);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/search/*", uAllFolderBySearchQuery);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName, uAllFolder);
    }

    private TableHelper mTableHelper;

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Todo replace with lazy injection
     */
    private void setHelper() {
        if (getContext() != null) {
            mTableHelper = ((MyApplication) getContext().getApplicationContext()).getTableHelper();
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (mTableHelper == null) {
            setHelper();
        }
        SQLiteDatabase sdb = mTableHelper.getReadableDatabase();
        Cursor c;
        switch (sUriMatcher.match(uri)) {

            //NOTES MATCH
            case uAllNotes:
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSingleNoteByNoteId:
                selection = table1.mUid + " =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllNoteByParentFolderName:
                selection = table1.mFolderName + " =?";
                String noteParentFolderName = uri.toString();
                noteParentFolderName = noteParentFolderName.substring(noteParentFolderName.lastIndexOf('/') + 1);
                selectionArgs = new String[]{noteParentFolderName};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllNoteBySearchQuery:
                selection = table1.mTitle + " LIKE ?";
                String noteSearchQuery = uri.toString();
                noteSearchQuery = noteSearchQuery.substring(noteSearchQuery.lastIndexOf('/') + 1);
                selectionArgs = new String[]{noteSearchQuery + "%"};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllNotesThatArePinned:
                selection = table1.mIsPinned + " =?";
                selectionArgs = new String[]{String.valueOf(1)};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllNotesThatAreLocked:
                selection = table1.mIsLocked + " =?";
                selectionArgs = new String[]{String.valueOf(1)};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllNotesByColor:
                selection = table1.mColor + " LIKE ?";
                String noteColorValue = uri.toString();
                noteColorValue = noteColorValue.substring(noteColorValue.lastIndexOf('/') + 1);
                selectionArgs = new String[]{noteColorValue};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;


            //FOLDER MATCH
            case uAllFolder:
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSingleFolderByFolderId:
                selection = TableNames.table2.mUid + " =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSingleFolderByName:
                selection = table2.mFolderName + " =?";
                String folderByName = uri.toString();
                folderByName = folderByName.substring(folderByName.lastIndexOf('/') + 1);
                selectionArgs = new String[]{folderByName};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFolderBySearchQuery:
                selection = table2.mFolderName + " LIKE ?";
                String folderSearchQuery = uri.toString();
                folderSearchQuery = folderSearchQuery.substring(folderSearchQuery.lastIndexOf('/') + 1);
                selectionArgs = new String[]{folderSearchQuery + "%"};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFolderByParentName:
                selection = table2.mParentFolderName + " =?";
                String folderByParentFolderName = uri.toString();
                folderByParentFolderName = folderByParentFolderName.substring(folderByParentFolderName.lastIndexOf('/') + 1);
                selectionArgs = new String[]{folderByParentFolderName};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFoldersThatArePinned:
                selection = table2.mIsPinned + " =?";
                selectionArgs = new String[]{String.valueOf(1)};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFoldersThatAreLocked:
                selection = table2.mIsLocked + " =?";
                selectionArgs = new String[]{String.valueOf(1)};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFoldersByColor:
                selection = table2.mFolderName + " =?";
                String folderColorValue = uri.toString();
                folderColorValue = folderColorValue.substring(folderColorValue.lastIndexOf('/') + 1);
                selectionArgs = new String[]{folderColorValue};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;


            default:
                throw new IllegalArgumentException("Invalid Uri " + uri);
        }
        if (getContext() != null && getContext().getContentResolver() != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)) {
            case uAllNotes:
                return insertVal(uri, contentValues, TableNames.mTableName);
            case uAllFolder:
                return insertVal(uri, contentValues, TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid Uri :" + uri);
        }
    }

    /**
     * @param u         the uri to be notified after change
     * @param cv        the values to insert
     * @param tableName the table to insert value into
     * @return the uri of new row
     */
    @Nullable
    private Uri insertVal(@NonNull Uri u, ContentValues cv, String tableName) {
        if (mTableHelper == null) {
            setHelper();
        }
        SQLiteDatabase sdb = mTableHelper.getWritableDatabase();
        long id = sdb.insert(tableName, null, cv);
        if (id == -1) {
            return null;
        } else {
            if (getContext() != null && getContext().getContentResolver() != null) {
                getContext().getContentResolver().notifyChange(u, null);
            }
            return Uri.withAppendedPath(u, String.valueOf(id));
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case uAllNotes:
                return deleteVal(uri, selection, selectionArgs, TableNames.mTableName);
            case uSingleNoteByNoteId:
                selection = table1.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteVal(uri, selection, selectionArgs, TableNames.mTableName);
            case uAllNoteByParentFolderName:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/') + 1);
                selectionArgs = new String[]{s};
                return deleteVal(uri, selection, selectionArgs, TableNames.mTableName);
            case uAllFolder:
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolderByFolderId:
                selection = TableNames.table2.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolderByName:
                selection = table2.mFolderName + " =?";
                String sk = uri.toString();
                sk = sk.substring(sk.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sk};
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            case uAllFolderByParentName:
                selection = table2.mParentFolderName + " =?";
                String sb = uri.toString();
                sb = sb.substring(sb.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sb};
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid uri" + uri);
        }
    }

    /**
     * @param u         the uri to be notified after deletion
     * @param sel       the column selected
     * @param selArgs   the condition
     * @param tableName the table on which delete operation will be performed
     * @return the index of deleted row
     */
    private int deleteVal(Uri u, String sel, String[] selArgs, String tableName) {
        if (mTableHelper == null) {
            setHelper();
        }
        SQLiteDatabase sdb = mTableHelper.getWritableDatabase();
        int count = sdb.delete(tableName, sel, selArgs);
        if (count > 0) {
            if (getContext() != null && getContext().getContentResolver() != null) {
                getContext().getContentResolver().notifyChange(Uri.withAppendedPath(u, String.valueOf(count)), null);
            }
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case uAllNotes:
                return updateVal(uri, values, selection, selectionArgs, TableNames.mTableName);
            case uSingleNoteByNoteId:
                selection = table1.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mTableName);
            case uAllNoteByParentFolderName:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/') + 1);
                selectionArgs = new String[]{s};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mTableName);
            case uAllFolder:
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolderByFolderId:
                selection = TableNames.table2.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolderByName:
                selection = table2.mFolderName + " =?";
                String sk = uri.toString();
                sk = sk.substring(sk.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sk};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            case uAllFolderByParentName:
                selection = table2.mParentFolderName + " =?";
                String sb = uri.toString();
                sb = sb.substring(sb.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sb};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid Uri" + uri);
        }
    }

    /**
     * @param u             the uri to be notified after updating
     * @param cv            the new value used for updating
     * @param selection     the column selected
     * @param selectionArgs the condition
     * @param tableName     the table on which update operation will be performed
     * @return the index of updated row
     */
    private int updateVal(@NonNull Uri u, ContentValues cv, String selection, String[] selectionArgs, String tableName) {
        if (mTableHelper == null) {
            setHelper();
        }
        SQLiteDatabase sdb = mTableHelper.getWritableDatabase();
        int count = sdb.update(tableName, cv, selection, selectionArgs);
        if (count == 0) {
            return 0;
        } else {
            if (getContext() != null && getContext().getContentResolver() != null) {
                getContext().getContentResolver().notifyChange(u, null);
            }
            return count;
        }
    }
}
