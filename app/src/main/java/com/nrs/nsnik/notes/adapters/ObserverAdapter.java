package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nrs.nsnik.notes.ContainerActivity;
import com.nrs.nsnik.notes.FileOperation;
import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderDataObserver;
import com.nrs.nsnik.notes.data.NoteDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.FolderCount;
import com.nrs.nsnik.notes.interfaces.NotesCount;
import com.nrs.nsnik.notes.interfaces.Observer;
import com.nrs.nsnik.notes.objects.NoteObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ObserverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Observer{

    private static final String TAG = ObserverAdapter.class.getSimpleName();
    private Context mContext;
    private List<NoteObject> mNotesList;
    private List<String> mFolderList;
    private List<Integer> mIds;
    private String mFolderName;
    private NotesCount mNotesCount;
    private FolderCount mFolderCount;
    private static final int NOTES = 0, FOLDER = 1;

    public ObserverAdapter(Context context, Uri noteUri, Uri folderUri, NotesCount notesCount,FolderCount folderCount, LoaderManager manager, String folderName){
        mContext = context;
        mFolderName = folderName;
        mIds = new ArrayList<>();
        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();
        mNotesCount = notesCount;
        mFolderCount = folderCount;
        NoteDataObserver noteDataObserver = new NoteDataObserver(mContext,noteUri,manager);
        FolderDataObserver folderDataObserver = new FolderDataObserver(mContext,folderUri,manager);
        noteDataObserver.add(this);
        folderDataObserver.add(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case FOLDER:
                return new FolderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_folder_layout, parent, false));
            case NOTES:
                return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_note_layout, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int pos = mFolderList.size();
        switch (holder.getItemViewType()){
            case FOLDER:
                bindFolderData(holder,position);
                break;
            case NOTES:
                pos = position-pos;
                bindNotesData(holder,pos);
                --pos;
                break;
        }
    }

    private void bindFolderData(RecyclerView.ViewHolder holder,int position){
        FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
        folderViewHolder.mFolderName.setText(mFolderList.get(position));
    }

    private void bindNotesData(RecyclerView.ViewHolder holder,int position){
        NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        NoteObject object = mNotesList.get(position);
        noteViewHolder.mNoteTitle.setText(object.getTitle());
        noteViewHolder.mNoteContent.setText(object.getNote());
        if (object.getImages().size() > 0) {
            noteViewHolder.mNoteImage.setImageBitmap(getImage(object));
            noteViewHolder.mNoteImage.setVisibility(View.VISIBLE);
        }
        if (object.getAudioLocation() != null) {
            noteViewHolder.mAudIndicator.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.mAudIndicator.setVisibility(View.GONE);
        }
        if (object.getReminder() != 0) {
            noteViewHolder.mRemIndicator.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.mRemIndicator.setVisibility(View.GONE);
        }
    }

    private Bitmap getImage(NoteObject object) {
        File folder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        File path = new File(folder, object.getImages().get(0));
        return BitmapFactory.decodeFile(path.toString());
    }

    @Override
    public int getItemCount() {
        return mNotesList.size()+mFolderList.size();
    }

    @Override
    public void updateItems(Cursor cursor) {
        if(cursor.getColumnIndex(TableNames.table1.mTitle)!=-1){
            makeNotesList(cursor);
        }else {
            makeFolderList(cursor);
        }
    }

    private void makeNotesList(Cursor cursor) {
        mNotesList.clear();
        mIds.clear();
        while (cursor != null && cursor.moveToNext()) {
            NoteObject object = new FileOperation(mContext).readFile(cursor.getString(cursor.getColumnIndex(TableNames.table1.mFileName)));
            if (object.getFolderName().equalsIgnoreCase(mFolderName)) {
                mIds.add(cursor.getInt(cursor.getColumnIndex(TableNames.table1.mUid)));
                mNotesList.add(object);
            }
        }
        mNotesCount.getNotesCount(mNotesList.size());
        notifyDataSetChanged();
    }

    private void makeFolderList(Cursor cursor) {
        mFolderList.clear();
        while (cursor != null && cursor.moveToNext()) {
            if(cursor.getString(cursor.getColumnIndex(TableNames.table2.mParentFolderName)).equalsIgnoreCase(mFolderName)) {
                mFolderList.add(justifyName(cursor.getString(cursor.getColumnIndex(TableNames.table2.mFolderName))));
            }
        }
        mFolderCount.getFolderCount(mFolderList.size());
        notifyDataSetChanged();
    }

    private String justifyName(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
    }


    @Override
    public int getItemViewType(int position) {
        if(position>=0&&position<mFolderList.size()){
            return FOLDER;
        }else if(position>=mFolderList.size()&&position<mNotesList.size()) {
            return NOTES;
        }
        return super.getItemViewType(position);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.singleNoteTitle) TextView mNoteTitle;
        @BindView(R.id.singleNoteContent) TextView mNoteContent;
        @BindView(R.id.singleNoteImage) ImageView mNoteImage;
        @BindView(R.id.singleNoteReminder) ImageView mRemIndicator;
        @BindView(R.id.singleNoteAudio) ImageView mAudIndicator;
        public NoteViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = mFolderList.size();
                    Intent intent = new Intent(mContext, NewNoteActivity.class);
                    intent.setData(Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(mIds.get(getAdapterPosition()-pos))));
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "noteTitle");
                    mContext.startActivity(intent, options.toBundle());
                }
            });
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.singleFolderName) TextView mFolderName;
        public FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ContainerActivity.class);
                    intent.putExtra(mContext.getResources().getString(R.string.intentFolderName), mFolderName.getText().toString());
                    mContext.startActivity(intent);
                }
            });
        }
    }

}
