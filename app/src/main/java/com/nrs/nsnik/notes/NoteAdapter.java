package com.nrs.nsnik.notes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nrs.nsnik.notes.data.TableNames.table1;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;


class NoteAdapter extends CursorAdapter{

    private Context mContext;

    NoteAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.single_note_layout,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView title,note;
        ImageView picture,reminderIndicator,audioIndicator;
        title = (TextView)view.findViewById(R.id.singleNoteTitle);
        note = (TextView) view.findViewById(R.id.sinleNoteContent);
        picture = (ImageView)view.findViewById(R.id.singleNoteImage) ;
        reminderIndicator = (ImageView)view.findViewById(R.id.singleNoteReminder);
        audioIndicator = (ImageView)view.findViewById(R.id.singleNoteAudio);
        title.setText(cursor.getString(cursor.getColumnIndex(table1.mTitile)));
        String filename = cursor.getString(cursor.getColumnIndex(table1.mFileName));
        try {
            note.setText(getNote(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(countImage(filename)>0){
                try {
                    picture.setImageBitmap(getImage(filename));
                    picture.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                };
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(checkAudio(filename)){
            audioIndicator.setVisibility(View.VISIBLE);
        }else {
            audioIndicator.setVisibility(View.GONE);
        }

        if(checkReminder(filename)){
            reminderIndicator.setVisibility(View.VISIBLE);
        }else {
            reminderIndicator.setVisibility(View.GONE);
        }
    }

    private NoteObject openObject(String filename){
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File f = new File(folder,filename);
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        NoteObject object = null;
        try {
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            object = (NoteObject) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    private boolean checkAudio(String filename){
        NoteObject object = openObject(filename);
        if(object.getAudioLocation()!=null){
            return true;
        }
        return false;
    }

    private boolean checkReminder(String filename){
        NoteObject object = openObject(filename);
        if(object.getReminder()!=0){
            return true;
        }
        return false;
    }

    private int countImage(String filename) throws IOException {
        NoteObject object = openObject(filename);
        return object.getImages().size();
    }

    private Bitmap getImage(String filename) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        NoteObject object = openObject(filename);
        File path = new File(folder,object.getImages().get(0));
        return BitmapFactory.decodeFile(path.toString());
    }

    private String getNote(String filename) throws IOException {
        NoteObject object = openObject(filename);
        return object.getNote();
    }


}
