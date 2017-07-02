package com.nrs.nsnik.notes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nrs.nsnik.notes.data.TableNames.table1;

class TableHelper extends SQLiteOpenHelper {

    //string representation of create table command on sqlite
    private static final String mCreateTable = "CREATE TABLE IF NOT EXISTS " + TableNames.mTableName + " ("
            + table1.mUid + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + table1.mTitle + " TEXT, "
            + table1.mFileName + " TEXT NOT NULL, "
            + table1.mFolderName + " TEXT, "
            + table1.mStarIndicator + " INTEGER "
            + ");";

    //string representation of drop table command on sqlite
    private static final String mDropTable = "DROP TABLE IF EXISTS " + TableNames.mTableName;

    //string representation of create table command on sqlite
    private static final String mCreateFolderTable = "CREATE TABLE " + TableNames.mFolderTableName + " ("
            + TableNames.table2.mUid + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TableNames.table2.mFolderName + " TEXT, "
            + TableNames.table2.mFolderId + " TEXT, "
            + TableNames.table2.mParentFolderName + " TEXT, "
            + TableNames.table2.mStarIndicator + " INTEGER "
            + ");";

    //string representation of create table command on sqlite
    private static final String mDropFolderTable = "DROP TABLE IF EXISTS " + TableNames.mFolderTableName;


    TableHelper(Context context) {
        super(context, TableNames.mDatabaseName, null, TableNames.mDataBaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    private void createTable(SQLiteDatabase sdb) {
        sdb.execSQL(mCreateTable);
        sdb.execSQL(mCreateFolderTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(mDropTable);
        sqLiteDatabase.execSQL(mDropFolderTable);
        createTable(sqLiteDatabase);
    }
}
