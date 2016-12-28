package com.nexus.nsnik.notes;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;


public class FileOperation {

    public static void saveText(Context c,String filename, String text) {
        File folder = new File(String.valueOf(c.getExternalFilesDir(c.getResources().getString(R.string.folder))));
        if (!folder.exists()) {
            folder.mkdir();
        }
        File f = new File(folder, filename);
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(text);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveImage(Context c,String filename, Bitmap image) {
        File folder = new File(String.valueOf(c.getExternalFilesDir(c.getResources().getString(R.string.folder))));
        if (!folder.exists()) {
            folder.mkdir();
        }
        File f = new File(folder, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            try {
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void readFile() {

    }


    private void deleteFile() {

    }


}
