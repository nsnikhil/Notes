package com.nexus.nsnik.notes.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nexus.nsnik.notes.data.TableNames.table1;



public class TableProvider extends ContentProvider{

    TableHelper tableHelper;

    private static final int uAllNotes = 111;
    private static final int uSingleNote = 112;
    private static final int uAllFolderNote = 212;
    private static final int uAllFolder = 113;
    private static final int uSingleFolder = 114;

    static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(TableNames.mAuthority,TableNames.mTableName,uAllNotes);
        sUriMatcher.addURI(TableNames.mAuthority,TableNames.mTableName+"/#",uSingleNote);
        sUriMatcher.addURI(TableNames.mAuthority,TableNames.mTableName+"/*",uAllFolderNote);
        sUriMatcher.addURI(TableNames.mAuthority,TableNames.mFolderTableName,uAllFolder);
        sUriMatcher.addURI(TableNames.mAuthority,TableNames.mFolderTableName+"/#",uSingleFolder);
    }

    @Override
    public boolean onCreate() {
        tableHelper = new TableHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)  {
        SQLiteDatabase sdb = tableHelper.getReadableDatabase();
        Cursor c = null;
        switch (sUriMatcher.match(uri)){
            case uAllNotes:
                c = sdb.query(TableNames.mTableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case uSingleNote:
                selection = table1.mUid + " =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = sdb.query(TableNames.mTableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case uAllFolderNote:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/')+1);
                selectionArgs = new String[]{s};
                c = sdb.query(TableNames.mTableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case uAllFolder:
                c = sdb.query(TableNames.mFolderTableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case uSingleFolder:
                selection = TableNames.table2.mUid+" =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                c = sdb.query(TableNames.mFolderTableName,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Invalid Uri"+uri);
        }
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)){
            case uAllNotes:
                return insertVal(uri,contentValues,TableNames.mTableName);
            case uAllFolder:
                return insertVal(uri,contentValues,TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid Uri :"+uri);
        }
    }

    private Uri insertVal(Uri u,ContentValues cv,String tableName){
        SQLiteDatabase sdb = tableHelper.getWritableDatabase();
        long id = sdb.insert(tableName,null,cv);
        if(id==-1){
            return null;
        }else {
            getContext().getContentResolver().notifyChange(u,null);
            return Uri.withAppendedPath(u,String.valueOf(id));
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sdb = tableHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case uAllNotes:
                getContext().getContentResolver().notifyChange(uri,null);
                return sdb.delete(TableNames.mTableName,selection,selectionArgs);
            case uSingleNote:
                selection = table1.mFolderName + " =?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return sdb.delete(TableNames.mTableName,selection,selectionArgs);
            case uAllFolderNote:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/')+1);
                selectionArgs = new String[]{s};
                getContext().getContentResolver().notifyChange(uri,null);
                return sdb.delete(TableNames.mTableName,selection,selectionArgs);
            case uAllFolder:
                getContext().getContentResolver().notifyChange(uri,null);
                return sdb.delete(TableNames.mFolderTableName,selection,selectionArgs);
            case uSingleFolder:
                selection = TableNames.table2.mUid+ "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri,null);
                return sdb.delete(TableNames.mFolderTableName,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Invalid uri"+uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (sUriMatcher.match(uri)){
            case uAllNotes:
                return updateVal(uri,values,selection,selectionArgs,TableNames.mTableName);
            case uSingleNote:
                selection = table1.mUid + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateVal(uri,values,selection,selectionArgs,TableNames.mTableName);
            case uAllFolderNote:
                selection = table1.mFolderName + " =?";
                String s = uri.toString();
                s = s.substring(s.lastIndexOf('/')+1);
                selectionArgs = new String[]{s};
                return updateVal(uri,values,selection,selectionArgs,TableNames.mTableName);
            case uAllFolder:
                return updateVal(uri,values,selection,selectionArgs,TableNames.mFolderTableName);
            case uSingleFolder:
                selection = TableNames.table2.mUid+ "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateVal(uri,values,selection,selectionArgs,TableNames.mFolderTableName);
            default:
                throw new IllegalArgumentException("Invalid Uri"+uri);
        }
    }

    private int updateVal(Uri u,ContentValues cv,String selection, String[] selectionArgs,String tableName){
        SQLiteDatabase sdb = tableHelper.getWritableDatabase();
        int count = sdb.update(tableName,cv,selection,selectionArgs);
        if(count==0){
            return 0;
        }else {
            getContext().getContentResolver().notifyChange(u,null);
            return count;
        }
    }
}
