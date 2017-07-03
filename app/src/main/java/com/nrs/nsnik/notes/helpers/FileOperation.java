package com.nrs.nsnik.notes.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.util.Random;


public class FileOperation {


    private Context mContext;
    private static final String TAG = FileOperation.class.getSimpleName();

    /*
    @param c    the context object
     */
    public FileOperation(Context c) {
        mContext = c;
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
        int count = mContext.getContentResolver().update(uri, contentValues, null, null);
        if (count == 0) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.updateFailed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.updateNote), Toast.LENGTH_SHORT).show();
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
        Uri u = mContext.getContentResolver().insert(TableNames.mContentUri, cv);
        if (u == null) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insertFailed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insertedNote), Toast.LENGTH_SHORT).show();
        }
    }

    /*
    @param fileName     the name of image file
    @param image        the image
     */
    public void saveImage(String fileName, Bitmap image) throws IOException {
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

    /*
    @param uri      the uri that will be used to get all the images and the file Name
                    of a note and then be deleted
     */
    public void deleteFile(Uri uri) throws IOException {
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
                    File path = new File(folder, obj.getImages().get(i));
                    isDeleted = path.delete();
                    if (path.exists() && !isDeleted) {
                        Log.d(TAG, "Error while deleting " + path.toString());
                    }
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
            Toast.makeText(mContext, "Database Error", Toast.LENGTH_LONG).show();
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
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
}
