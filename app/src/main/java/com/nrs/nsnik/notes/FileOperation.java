package com.nrs.nsnik.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.data.TableNames.table1;
import com.nrs.nsnik.notes.objects.NoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class FileOperation {

    private Context mContext;

    public FileOperation(Context c) {
        mContext = c;
    }

    void saveNote(String filename, NoteObject noteObject) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File f = new File(folder, filename);
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
        insertInTable(filename, noteObject);
    }

    void updateNote(String filename, NoteObject noteObject, Uri uri) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File f = new File(folder, filename);
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

    private void insertInTable(String filename, NoteObject obj) {
        ContentValues cv = new ContentValues();
        cv.put(table1.mTitle, obj.getTitle());
        cv.put(table1.mFileName, filename);
        cv.put(table1.mFolderName, obj.getFolderName());
        Uri u = mContext.getContentResolver().insert(TableNames.mContentUri, cv);
        if (u == null) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insertFailed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insertedNote), Toast.LENGTH_SHORT).show();
        }
    }

    void saveImage(String filename, Bitmap image) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File f = new File(folder, filename);
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

    public NoteObject readFile(String filename) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File f = new File(folder, filename);
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        NoteObject object = null;
        try {
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            object = (NoteObject) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(fis!=null){fis.close();}
            if(ois!=null){ois.close();}
        }
        return object;
    }


    void deleteFile(Uri uri) throws IOException {
        Cursor c = mContext.getContentResolver().query(uri, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
            File f = new File(folder, c.getString(c.getColumnIndex(table1.mFileName)));
            FileInputStream fis = null;
            ObjectInputStream ois = null;
            try {
                fis = new FileInputStream(f);
                ois = new ObjectInputStream(fis);
                NoteObject obj = (NoteObject) ois.readObject();
                for (int i = 0; i < obj.getImages().size(); i++) {
                    File path = new File(folder, obj.getImages().get(i));
                    path.delete();
                }
                File file = new File(folder, c.getString(c.getColumnIndex(table1.mFileName)));
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (ois != null) {
                    ois.close();
                }
                c.close();
            }
        }
    }

}
