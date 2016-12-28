package com.nexus.nsnik.notes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.nexus.nsnik.notes.data.TableNames.table1;

public class TableHelper extends SQLiteOpenHelper{

    private static final String mCreateTable = "CREATE TABLE IF NOT EXISTS " + TableNames.mTableName + " ("
            + table1.mUid + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + table1.mTitile + " VARCHAR(250), "
            + table1.mNote + " VARCHAR(250) NOT NULL, "
            + table1.mPicture0 + " TEXT, "
            + table1.mPicture1 + " TEXT, "
            + table1.mPicture2 + " TEXT, "
            + table1.mPicture3 + " TEXT, "
            + table1.mPicture4 + " TEXT, "
            + table1.mPicture5 + " TEXT, "
            + table1.mPicture6 + " TEXT, "
            + table1.mPicture7 + " TEXT, "
            + table1.mPicture8 + " TEXT, "
            + table1.mPicture9 + " TEXT, "
            + table1.mAudio+ " TEXT, "
            + table1.mReminder + " INTEGER, "
            + table1.mFolderName + " TEXT "
            + ");";

    private static final String mDropTable = "DROP TABLE IF EXISTS "+ TableNames.mTableName;

    private static final String mCreateFolderTable = "CREATE TABLE "+ TableNames.mFolderTableName + " ("
            + TableNames.table2.mUid + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TableNames.table2.mFolderName + " VARCHAR(250), "
            + TableNames.table2.mFolderId + " TEXT "
            + ");";


    private static final String mDropFolderTable = "DROP TABLE IF EXISTS "+ TableNames.mFolderTableName;


    public TableHelper(Context context) {
        super(context, TableNames.mDatabaseName, null, TableNames.mDataBaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    private void createTable(SQLiteDatabase sdb){
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
