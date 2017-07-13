/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.helpers;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.data.TableNames.table1;
import com.nrs.nsnik.notes.objects.NoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Random;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class FileOperation {


    private static final String TAG = FileOperation.class.getSimpleName();
    private Context mContext;
    private AsyncQueryHandler mAsyncQueryHandler;

    /*
    @param c    the context object
     */
    public FileOperation(Context c) {
        mContext = c;
    }

    /*
    @param c                    the context object
    @param requireAsyncDb       boolean which indicates if the classes need asyncdb
     */
    @SuppressLint("HandlerLeak")
    public FileOperation(Context context, boolean requireAsyncDb) {
        mContext = context;
        mAsyncQueryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {
            @Override
            protected Handler createHandler(Looper looper) {
                return super.createHandler(looper);
            }

            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                if (uri == null) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.insertFailed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.insertedNote), Toast.LENGTH_SHORT).show();
                }
                super.onInsertComplete(token, cookie, uri);
            }

            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {
                if (result == 0) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.updateFailed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.updateNote), Toast.LENGTH_SHORT).show();
                }
                super.onUpdateComplete(token, cookie, result);
            }

            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                super.onDeleteComplete(token, cookie, result);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
            }
        };
    }

    /*
    @param fileName     the name by which the file wil be saved
                        each new notes has a different file name
    @param noteObject   the object that is written to file
     */
    public void saveNote(String fileName, NoteObject noteObject) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File f = new File(folder, fileName);
        try {
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(noteObject);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (oos != null) {
                oos.close();
            }
        }
        insertInTable(fileName, noteObject);
    }

    /*
    @param fileName         the name by which the file wil be saved
    @param noteObject       the object that will be written to file
    @param uri              the uri ton which the update operation will be
                            performed
     */
    public void updateNote(String fileName, NoteObject noteObject, Uri uri) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File f = new File(folder, fileName);
        try {
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(noteObject);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (oos != null) {
                oos.close();
            }
        }
        updateInTable(noteObject.getTitle(), uri);
    }

    /*
    @param title    the title of the note to be updated
    @param uri      the uri on which update operation will be performed
     */
    private void updateInTable(String title, Uri uri) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(table1.mTitle, title);
        mAsyncQueryHandler.startUpdate(1, null, uri, contentValues, null, null);
    }

    public void deleteAudioFile(String fileName) {
        File folder = new File(String.valueOf(mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName))));
        File f = new File(folder, fileName);
        boolean isDeleted = false;
        if (f.exists()) {
            isDeleted = f.delete();
        }
        if (!isDeleted) {
            Log.d(TAG, "Error while deleting " + f.toString());
        }
    }

    /*
    @param fileName     the name of the fie which contains the note object
    @param noteObject   the note object that represents a single note
     */
    private void insertInTable(String fileName, NoteObject noteObject) {
        ContentValues cv = new ContentValues();
        cv.put(table1.mTitle, noteObject.getTitle());
        cv.put(table1.mFileName, fileName);
        cv.put(table1.mFolderName, noteObject.getFolderName());
        mAsyncQueryHandler.startInsert(1, null, TableNames.mContentUri, cv);
    }

    /*
    @param fileName     the name of image file
    @param image        the image
     */
    public void saveImage(String fileName, Bitmap image) {
        Completable completable = Completable.fromCallable(() -> {
            File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
            File f = new File(folder, fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) fos.close();
            }
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                Timber.d("Image Saved");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d(TAG, e.getMessage());
            }
        });
    }

    /*
    @param fileName     the name of that file that contains a note object
     */
    public NoteObject readFile(String fileName) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File f = new File(folder, fileName);
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        NoteObject object = null;
        try {
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            object = (NoteObject) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return object;
    }

    public void deleteImage(String imageFileName) {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File imageFile = new File(folder, imageFileName);
        boolean isDeleted = false;
        if (imageFile.exists()) {
            isDeleted = imageFile.delete();
        }
        if (imageFile.exists() && !isDeleted) {
            Timber.d("Error while deleting " + imageFile.toString());
        }
    }

    /*
    @param uri      the uri that will be used to get all the images and the file Name
                    of a note and then be deleted
     */
    private void deleteFileBack(Uri uri) throws IOException {
        Cursor c = mContext.getContentResolver().query(uri, null, null, null, null);
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        boolean isDeleted;
        try {
            while (c != null && c.moveToNext()) {
                File f = new File(folder, c.getString(c.getColumnIndex(table1.mFileName)));
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                NoteObject obj = (NoteObject) ois.readObject();
                for (int i = 0; i < obj.getImages().size(); i++) {
                    deleteImage(obj.getImages().get(i));
                }
                for (int i = 0; i < obj.getAudioLocations().size(); i++) {
                    deleteAudioFile(obj.getAudioLocations().get(i));
                }
                isDeleted = f.delete();
                if (f.exists() && !isDeleted) {
                    Log.d(TAG, "Error while deleting " + f.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (c != null) {
                c.close();
            }
        }
    }

    /*
    @param uri  the uri of the bot that is to be deleted

    first the resources related to note are deleted by calling the function
    @function deleteFileBack(Uri) then the note data is deleted from database
     */
    public void deleteNote(Uri uri) {
        Completable completable = Completable.fromCallable(() -> {
            deleteFileBack(uri);
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                mAsyncQueryHandler.startDelete(0, null, uri, null, null);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.getMessage());
            }
        });
    }

    /*
    @param uri      the uri of the folder that is to be deleted
    @param folderName   the name of the folder that is to deleted

    folder name is to delete all the notes and their
    resources that arw within that folder, first the
    resources related to all notes in folder is deleted then
    all the notes in the database that are stored in that
    folder and finally the folder
     */
    public void deleteFolder(Uri uri, String folderName) {
        Completable completable = Completable.fromCallable(() -> {
            deleteFileBack(Uri.withAppendedPath(TableNames.mContentUri, folderName));
            return null;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                mAsyncQueryHandler.startDelete(0, null, Uri.withAppendedPath(TableNames.mContentUri, folderName), null, null);
                mAsyncQueryHandler.startDelete(0, null, uri, null, null);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.getMessage());
            }
        });
    }

    /*

    This method takes id of two notes and swaps them by
    first assigning a temporary id to them and then finally swapping
    the temporary with the actual

    @param fromId       the id of the first note
    @param toId         the id of second note
     */
    public void switchNoteId(int fromId, int toId) {
        if (fromId != -1 && toId != -1) {
            int tempFromId = generateRandom(9999, 5000), tempToID = generateRandom(4999, 1000);

            //Change actual to temp

            Uri fromUri = Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(fromId));
            Uri toUri = Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(toId));

            ContentValues fromContentValues = new ContentValues();
            fromContentValues.put(table1.mUid, tempFromId);

            ContentValues toContentValues = new ContentValues();
            toContentValues.put(table1.mUid, tempToID);

            mContext.getContentResolver().update(fromUri, fromContentValues, null, null);
            mContext.getContentResolver().update(toUri, toContentValues, null, null);


            //Change temp To Actual

            Uri newFomUri = Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(tempFromId));
            Uri newToUri = Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(tempToID));

            ContentValues newFromContentValues = new ContentValues();
            newFromContentValues.put(table1.mUid, toId);

            ContentValues newToContentValues = new ContentValues();
            newToContentValues.put(table1.mUid, fromId);

            mContext.getContentResolver().update(newFomUri, newFromContentValues, null, null);
            mContext.getContentResolver().update(newToUri, newToContentValues, null, null);
        } else {
            Log.d(TAG, "Database Error");
        }
    }

    /*
    This method takes id of two folders and swaps them by
    first assigning a temporary id to them and then finally swapping
    the temporary with the actual

    @param fromId       the id of the first folder
    @param toId         the id of second folder
     */
    public void switchFolderId(int fromId, int toId) {

        if (fromId != -1 && toId != -1) {

            int tempFromId = generateRandom(9999, 5000), tempToID = generateRandom(4999, 1000);

            //Change actual to temp

            Uri fromUri = Uri.withAppendedPath(TableNames.mFolderContentUri, String.valueOf(fromId));
            Uri toUri = Uri.withAppendedPath(TableNames.mFolderContentUri, String.valueOf(toId));

            //8336085805
            ContentValues fromContentValues = new ContentValues();
            fromContentValues.put(TableNames.table2.mUid, tempFromId);

            ContentValues toContentValues = new ContentValues();
            toContentValues.put(TableNames.table2.mUid, tempToID);


            mContext.getContentResolver().update(fromUri, fromContentValues, null, null);
            mContext.getContentResolver().update(toUri, toContentValues, null, null);


            //Change temp To Actual

            Uri newFomUri = Uri.withAppendedPath(TableNames.mFolderContentUri, String.valueOf(tempFromId));
            Uri newToUri = Uri.withAppendedPath(TableNames.mFolderContentUri, String.valueOf(tempToID));

            ContentValues newFromContentValues = new ContentValues();
            newFromContentValues.put(TableNames.table2.mUid, toId);

            ContentValues newToContentValues = new ContentValues();
            newToContentValues.put(TableNames.table2.mUid, fromId);


            mContext.getContentResolver().update(newFomUri, newFromContentValues, null, null);
            mContext.getContentResolver().update(newToUri, newToContentValues, null, null);
        } else {
            Log.d(TAG, "Database Error");
        }
    }

    /*
    this method takes a uri and
    provides the id of a note or folder in
    particular position

    @param uri          the uri which will be searched
    @param position     the position at which the search will end
     */
    public int getId(Uri uri, int position) {
        int uid = -1;
        Cursor tempCursor = mContext.getContentResolver().query(uri, null, null, null, null);
        try {
            if (tempCursor != null) {
                for (int i = 0; i <= tempCursor.getCount(); i++) {
                    if (i <= position && tempCursor.moveToNext()) {
                        uid = tempCursor.getInt(tempCursor.getColumnIndex(TableNames.table1.mUid));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (tempCursor != null) {
                tempCursor.close();
            }
        }
        return uid;
    }

    /*
     takes max and min and finds a random no
     between that range

    @param max      the maximum range value
    @param min      the minimum range value
     */
    private int generateRandom(int max, int min) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public Bitmap mCompressBitmap(Bitmap bitmap) {
        return null;
    }

    public String makeName(FILE_TYPES type) {
        Calendar c = Calendar.getInstance();
        switch (type) {
            case TEXT:
                return c.getTimeInMillis() + ".txt";
            case IMAGE:
                return c.getTimeInMillis() + ".jpg";
            case AUDIO:
                return c.getTimeInMillis() + ".3gp";
            default:
                throw new IllegalArgumentException("Invalid type " + type.toString());
        }
    }

    public enum FILE_TYPES {TEXT, IMAGE, AUDIO}
}
