package com.nrs.nsnik.notes.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class TableNames {

    public static final String mDatabaseName = "newnotedatabase";
    public static final int mDataBaseVersion = 6;
    public static final String mTableName = "newnotetable";
    public static final String mFolderTableName = "foldertable";

    public static final String mScheme = "content://";
    public static final String mAuthority = "com.nrs.nsnik.notes";


    public static final Uri mBaseUri = Uri.parse(mScheme + mAuthority);
    public static final Uri mContentUri = Uri.withAppendedPath(mBaseUri, mTableName);
    public static final Uri mFolderContentUri = Uri.withAppendedPath(mBaseUri, mFolderTableName);


    public class table1 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mTitle = "title";
        public static final String mFileName = "filename";
        public static final String mFolderName = "foldername";
    }

    public class table2 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mFolderName = "foldername";
        public static final String mFolderId = "folderid";
    }
}
