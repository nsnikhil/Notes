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


    public class table1 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mTitle = "title";
        public static final String mFileName = "filename";
        public static final String mFolderName = "foldername";
        public static final String mStarIndicator = "isStarred";
    }

    public class table2 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mFolderName = "foldername";
        public static final String mFolderId = "folderid";
        public static final String mParentFolderName = "parfoldername";
        public static final String mStarIndicator = "isStarred";
    }
}
