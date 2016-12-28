package com.nexus.nsnik.notes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.nexus.nsnik.notes.data.TableNames.table1;
import java.io.File;
import java.io.IOException;


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
        String noteBody = getNote(cursor);
        if (noteBody!=null){
            note.setText(noteBody);
        }
        setImage(picture,cursor,context);
        if(cursor.getInt(cursor.getColumnIndex(table1.mReminder))==1){
            reminderIndicator.setVisibility(View.VISIBLE);
        }else {
            reminderIndicator.setVisibility(View.GONE);
        }
        if(cursor.getString(cursor.getColumnIndex(table1.mAudio))!=null){
            audioIndicator.setVisibility(View.VISIBLE);
        }else {
            audioIndicator.setVisibility(View.GONE);
        }
    }

    private String getNote(Cursor cursor) {
        String note = null;
        File folder = new File(String.valueOf(mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName))));
        File f = new File(folder,cursor.getString(cursor.getColumnIndex(table1.mNote)));
        try {
            note = Files.toString(f, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return note;
    }

    private void setImage(ImageView iv,Cursor cursor,Context context){
        if (cursor.getString(cursor.getColumnIndex(table1.mPicture0))!=null){
            File folder = new File(String.valueOf(context.getExternalFilesDir(context.getResources().getString(R.string.folderName))));
            File f = new File(folder,cursor.getString(cursor.getColumnIndex(table1.mPicture0)));
            String fpath = String.valueOf(f);
            Bitmap bp = BitmapFactory.decodeFile(fpath);
            iv.setImageBitmap(bp);
            iv.setVisibility(View.VISIBLE);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else {
            iv.setVisibility(View.GONE);
        }
    }
}
