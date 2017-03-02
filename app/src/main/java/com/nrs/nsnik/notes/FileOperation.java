package com.nrs.nsnik.notes;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.data.TableNames.table1;



public class FileOperation {

    Context mContext;

    FileOperation(Context c){
        mContext = c;
    }

    public void saveNote(String filename,NoteObject noteObject) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File f = new File(folder,filename);
        try{
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(noteObject);
            oos.flush();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(fos!=null){
                fos.close();
            }
            if(oos!=null){
                oos.close();
            }
        }
        insertInTable(filename,noteObject);
    }

    public void updateNote(String filename,NoteObject noteObject,Uri uri) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        File f = new File(folder,filename);
        try{
            fos = new FileOutputStream(f);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(noteObject);
            oos.flush();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(fos!=null){
                fos.close();
            }
            if(oos!=null){
                oos.close();
            }
        }
        updateInTable(noteObject.getTitle(),uri);
    }

    private void updateInTable(String title,Uri uri){
        ContentValues contentValues  = new ContentValues();
        contentValues.put(table1.mTitile,title);
        int count = mContext.getContentResolver().update(uri,contentValues,null,null);
        if (count == 0) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.updateFailed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.updateNote), Toast.LENGTH_SHORT).show();
        }
    }

    public void insertInTable(String filename,NoteObject obj){
        ContentValues cv = new ContentValues();
        cv.put(table1.mTitile, obj.getTitle());
        cv.put(table1.mFileName, filename);
        cv.put(table1.mFolderName, obj.getFolderName());
        Uri u = mContext.getContentResolver().insert(TableNames.mContentUri, cv);
        if (u == null) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insertFailed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.insertedNote), Toast.LENGTH_SHORT).show();
        }
    }


    public  void saveImage(String filename, Bitmap image) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File f = new File(folder,filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            image.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            fos.close();
        }
    }


    private void readFile() {

    }


    private void deleteFile() {

    }


}
