package com.nrs.nsnik.notes;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nrs.nsnik.notes.data.TableNames.table2;

public class FolderAdapter extends CursorAdapter{

    Context mContext;

    public FolderAdapter(Context context, Cursor c) {
        super(context, c);
        mContext  = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_folder_layout,parent,false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView folderName = (TextView)view.findViewById(R.id.singleFolderName);
        folderName.setText(cursor.getString(cursor.getColumnIndex(table2.mFolderName)));
    }
}
