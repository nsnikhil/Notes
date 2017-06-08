package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.NoteDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.NotesCount;
import com.nrs.nsnik.notes.interfaces.Observer;
import com.nrs.nsnik.notes.objects.NoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NoteObserverAdapter extends RecyclerView.Adapter<NoteObserverAdapter.MyViewHolder> implements Observer {

    private NotesCount mCount;
    private Context mContext;
    private List<NoteObject> mNotesList;
    private List<Integer> mIds;
    private NoteDataObserver observer;
    private static final String TAG  = NoteDataObserver.class.getSimpleName();


    public NoteObserverAdapter(Context context,Uri uri, LoaderManager manager,NotesCount count) {
        mContext = context;
        observer = new NoteDataObserver(mContext,uri, manager);
        mNotesList = new ArrayList<>();
        mIds = new ArrayList<>();
        mCount = count;
        observer.add(this);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_note_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NoteObject object = mNotesList.get(position);
        holder.mNoteTitle.setText(object.getTitle());
        holder.mNoteContent.setText(object.getNote());
        if (object.getImages().size() > 0) {
            try {
                holder.mNoteImage.setImageBitmap(getImage(object));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (object.getAudioLocation() != null) {
            holder.mAudIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mAudIndicator.setVisibility(View.GONE);
        }
        if (object.getReminder() != 0) {
            holder.mRemIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mRemIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mNotesList.size();
    }

    public NoteObject getSingleItem(int position){
        return mNotesList.get(position);
    }

    public void modifySingle(NoteObject object, int position) {
        mNotesList.add(position, object);
        notifyItemChanged(position);
    }

    public void modifyAll(List<NoteObject> objects) {
        mNotesList = objects;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mNotesList.remove(position);
        notifyItemChanged(position);
    }

    @Override
    public void updateItems(Cursor cursor) {
        makeNotesList(cursor);
    }

    private void makeNotesList(Cursor cursor) {
        mNotesList.clear();
        while (cursor != null && cursor.moveToNext()) {
            mIds.add(cursor.getInt(cursor.getColumnIndex(TableNames.table1.mUid)));
            NoteObject object = openObject(cursor.getString(cursor.getColumnIndex(TableNames.table1.mFileName)));
            mNotesList.add(object);
        }
        mCount.getNotesCount(mNotesList.size());
        notifyDataSetChanged();
    }

    private NoteObject openObject(String filename) {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File f = new File(folder, filename);
        FileInputStream fis;
        ObjectInputStream ois ;
        NoteObject object = null;
        try {
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            object = (NoteObject) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    private Bitmap getImage(NoteObject object) throws IOException {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File path = new File(folder, object.getImages().get(0));
        return BitmapFactory.decodeFile(path.toString());
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleNoteTitle)
        TextView mNoteTitle;
        @BindView(R.id.singleNoteContent)
        TextView mNoteContent;
        @BindView(R.id.singleNoteImage)
        ImageView mNoteImage;
        @BindView(R.id.singleNoteReminder)
        ImageView mRemIndicator;
        @BindView(R.id.singleNoteAudio)
        ImageView mAudIndicator;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NewNoteActivity.class);
                    intent.setData(Uri.withAppendedPath(TableNames.mContentUri,String.valueOf(mIds.get(getAdapterPosition()))));
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
