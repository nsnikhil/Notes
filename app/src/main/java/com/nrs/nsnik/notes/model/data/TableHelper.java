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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.nrs.nsnik.notes.model.dagger.qualifiers.ApplicationQualifier;
import com.nrs.nsnik.notes.model.data.TableNames.table1;
import com.nrs.nsnik.notes.model.data.TableNames.table2;

import javax.inject.Inject;

public class TableHelper extends SQLiteOpenHelper {

    //string representation of create table command on sqlite
    private static final String mCreateTable = "CREATE TABLE IF NOT EXISTS " + TableNames.mTableName + " ("
            + table1.mUid + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + table1.mTitle + " TEXT, "
            + table1.mFileName + " TEXT NOT NULL, "
            + table1.mFolderName + " TEXT, "
            + table1.mIsPinned + " INTEGER DEFAULT 0, "
            + table1.mIsLocked + " INTEGER DEFAULT 0, "
            + table1.mDataModified + " TEXT, "
            + table1.mColor + " TEXT "
            + ");";

    //string representation of drop table command on sqlite
    private static final String mDropTable = "DROP TABLE IF EXISTS " + TableNames.mTableName;

    //string representation of create table command on sqlite
    private static final String mCreateFolderTable = "CREATE TABLE IF NOT EXISTS " + TableNames.mFolderTableName + " ("
            + table2.mUid + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + table2.mFolderName + " TEXT, "
            + table2.mFolderId + " TEXT, "
            + table2.mParentFolderName + " TEXT, "
            + table2.mIsPinned + " INTEGER DEFAULT 0, "
            + table2.mIsLocked + " INTEGER DEFAULT 0, "
            + table2.mColor + " TEXT "
            + ");";

    //string representation of create table command on sqlite
    private static final String mDropFolderTable = "DROP TABLE IF EXISTS " + TableNames.mFolderTableName;

    @Inject
    TableHelper(@ApplicationQualifier Context context) {
        super(context, TableNames.mDatabaseName, null, TableNames.mDataBaseVersion);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    private void createTable(@NonNull SQLiteDatabase sdb) {
        sdb.execSQL(mCreateTable);
        sdb.execSQL(mCreateFolderTable);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(mDropTable);
        sqLiteDatabase.execSQL(mDropFolderTable);
        createTable(sqLiteDatabase);
    }
}
