package com.nrs.nsnik.notes.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class TableNames {

    static final String mDatabaseName = "newnotedatabase";
    static final int mDataBaseVersion = 8;
    static final String mTableName = "newnotetable";
    static final String mFolderTableName = "foldertable";
    static final String mAuthority = "com.nrs.nsnik.notes";
    private static final String mScheme = "content://";
    private static final Uri mBaseUri = Uri.parse(mScheme + mAuthority);
    public static final Uri mContentUri = Uri.withAppendedPath(mBaseUri, mTableName);
    public static final Uri mFolderContentUri = Uri.withAppendedPath(mBaseUri, mFolderTableName);

    /*
    @mUid           column representing the id of each element in note table
    @mTitle         column representing the title of each note
    @mFileName      column representing the file name of the note object
    @FolderName     column representing the folder name of the each note
    @StarIndicator  column representing the star value of the each note
     */
    public class table1 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mTitle = "title";
        public static final String mFileName = "filename";
        public static final String mFolderName = "foldername";
        public static final String mStarIndicator = "isStarred";
    }

    /*
     @mUid                column representing the id of each element in folder table
     @mFolderName         column representing the folder name of folder
     @mFolderId           column representing the folder id name of the folder
     @mParentFolderName   column representing the parent folder name of the folder
     @StarIndicator       column representing the star value of the each folder
    */
    public class table2 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mFolderName = "foldername";
        public static final String mFolderId = "folderid";
        public static final String mParentFolderName = "parfoldername";
        public static final String mStarIndicator = "isStarred";
    }
}
