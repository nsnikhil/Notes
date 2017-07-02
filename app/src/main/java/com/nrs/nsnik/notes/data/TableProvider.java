package com.nrs.nsnik.notes.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nrs.nsnik.notes.data.TableNames.table1;
import com.nrs.nsnik.notes.data.TableNames.table2;


public class TableProvider extends ContentProvider {

    /*
    TODO  WRITE DOCUMENTATION FOR PROVIDER CLASS
     */

    private static final String TAG = TableProvider.class.getSimpleName();
    private static final int uAllNotes = 111;
    private static final int uSingleNote = 112;
    private static final int uAllFolderNote = 113;
    private static final int uAllSearchNote = 114;
    private static final int uAllFolder = 213;
    private static final int uSingleFolder = 214;
    private static final int uAllSubFolder = 215;
    private static final int uSingleFolderName = 216;
    private static final int uSearchFolderName = 217;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName, uAllNotes);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/#", uSingleNote);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/search/*", uAllSearchNote);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mTableName + "/*", uAllFolderNote);

        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName, uAllFolder);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/#", uSingleFolder);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/search/*", uSearchFolderName);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/*", uSingleFolderName);
        sUriMatcher.addURI(TableNames.mAuthority, TableNames.mFolderTableName + "/*", uAllSubFolder);
    }

    private TableHelper tableHelper;

    @Override
    public boolean onCreate() {
        tableHelper = new TableHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase sdb = tableHelper.getReadableDatabase();
        Cursor c;
        switch (sUriMatcher.match(uri)) {
            case uAllNotes:
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSingleNote:
                selection = table1.mUid + " =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFolderNote:
                selection = table1.mFolderName + " =?";
                String sp = uri.toString();
                sp = sp.substring(sp.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sp};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllSearchNote:
                selection = table1.mTitle + " LIKE ?";
                String srn = uri.toString();
                srn = srn.substring(srn.lastIndexOf('/') + 1);
                selectionArgs = new String[]{srn + "%"};
                c = sdb.query(TableNames.mTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllFolder:
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSingleFolder:
                selection = TableNames.table2.mUid + " =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSingleFolderName:
                selection = table2.mFolderName + " =?";
                String sk = uri.toString();
                sk = sk.substring(sk.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sk};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uSearchFolderName:
                selection = table2.mFolderName + " LIKE ?";
                String src = uri.toString();
                src = src.substring(src.lastIndexOf('/') + 1);
                selectionArgs = new String[]{src + "%"};
                c = sdb.query(TableNames.mFolderTableName, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case uAllSubFolder:
                selection = table2.mParentFolderName + " =?";
                String sf = uri.toString();
                sf = sf.substring(sf.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sf};
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

    private Uri insertVal(Uri u, ContentValues cv, String tableName) {
        SQLiteDatabase sdb = tableHelper.getWritableDatabase();
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
            case uSingleNote:
                selection = table1.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteVal(uri, selection, selectionArgs, TableNames.mTableName);
            case uAllFolderNote:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/') + 1);
                selectionArgs = new String[]{s};
                return deleteVal(uri, selection, selectionArgs, TableNames.mTableName);
            case uAllFolder:
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolder:
                selection = TableNames.table2.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolderName:
                selection = table2.mFolderName + " =?";
                String sk = uri.toString();
                sk = sk.substring(sk.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sk};
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            case uAllSubFolder:
                selection = table2.mParentFolderName + " =?";
                String sb = uri.toString();
                sb = sb.substring(sb.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sb};
                return deleteVal(uri, selection, selectionArgs, TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid uri" + uri);
        }
    }

    private int deleteVal(Uri u, String sel, String[] selArgs, String tableName) {
        SQLiteDatabase sdb = tableHelper.getWritableDatabase();
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
            case uSingleNote:
                selection = table1.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mTableName);
            case uAllFolderNote:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/') + 1);
                selectionArgs = new String[]{s};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mTableName);
            case uAllFolder:
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolder:
                selection = TableNames.table2.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            case uSingleFolderName:
                selection = table2.mFolderName + " =?";
                String sk = uri.toString();
                sk = sk.substring(sk.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sk};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            case uAllSubFolder:
                selection = table2.mParentFolderName + " =?";
                String sb = uri.toString();
                sb = sb.substring(sb.lastIndexOf('/') + 1);
                selectionArgs = new String[]{sb};
                return updateVal(uri, values, selection, selectionArgs, TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid Uri" + uri);
        }
    }

    private int updateVal(Uri u, ContentValues cv, String selection, String[] selectionArgs, String tableName) {
        SQLiteDatabase sdb = tableHelper.getWritableDatabase();
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
