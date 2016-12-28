package com.nexus.nsnik.notes.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class TableNames {

    public static final String mDatabaseName = "newnotedatabase";
    public static final int mDataBaseVersion = 2;
    public static final String mTableName = "newnotetable";
    public static final String mFolderTableName = "foldertable";

    public static final String mScheme = "content://";
    public static final String mAuthority = "com.nexus.nsnik.notes";


    public static final Uri mBaseUri = Uri.parse(mScheme + mAuthority);
    public static final Uri mContentUri = Uri.withAppendedPath(mBaseUri, mTableName);
    public static final Uri mFolderContentUri = Uri.withAppendedPath(mBaseUri, mFolderTableName);


    public class table1 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mTitile = "title";
        public static final String mNote = "note";
        public static final String mPicture0 = "picturea";
        public static final String mPicture1 = "pictureb";
        public static final String mPicture2 = "picturec";
        public static final String mPicture3 = "pictured";
        public static final String mPicture4 = "picturee";
        public static final String mPicture5 = "picturef";
        public static final String mPicture6 = "pictureg";
        public static final String mPicture7 = "pictureh";
        public static final String mPicture8 = "picturei";
        public static final String mPicture9 = "picturej";
        public static final String mAudio = "audio";
        public static final String mReminder = "reminder";
        public static final String mFolderName = "foldername";
    }

    public class table2 implements BaseColumns {
        public static final String mUid = BaseColumns._ID;
        public static final String mFolderName = "foldername";
        public static final String mFolderId = "folderid";
    }
}
