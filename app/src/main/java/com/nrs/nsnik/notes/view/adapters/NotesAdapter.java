/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.util.FileUtil;
import com.nrs.nsnik.notes.view.listeners.ItemTouchListener;
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * This adapter takes in a uri queries the uri and
 * registers for change in uri i.e. if any items in
 * that uri changes the adapter is notified and then
 * adapters clear the old list re draws the entire list
 * with the new data set
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchListener {

    private static final int NOTES = 0, FOLDER = 1, HEADER = 2;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final RequestManager mRequestManager;
    private final FileUtil mFileUtil;
    private final CompositeDisposable mCompositeDisposable;
    private final File mRootFolder;
    private final NoteItemClickListener mNoteItemClickListener;
    private List<NoteEntity> mNotesList;
    private List<FolderEntity> mFolderList;

    /*
    TODO ENABLE SHOW TRANSITIONING OF LAYOUT CHANGES
     */

    /**
     * @param context    The context object
     * @param noteList   Note list
     * @param folderList Folder List
     */
    public NotesAdapter(Context context, @NonNull List<NoteEntity> noteList, @NonNull List<FolderEntity> folderList,
                        NoteItemClickListener noteItemClickListener) {
        mContext = context;
        mNotesList = noteList;
        mFolderList = folderList;
        mNoteItemClickListener = noteItemClickListener;

        mRequestManager = ((MyApplication) mContext.getApplicationContext()).getRequestManager();
        mFileUtil = ((MyApplication) mContext.getApplicationContext()).getFileUtil();
        mRootFolder = mFileUtil.getRootFolder();

        mLayoutInflater = LayoutInflater.from(mContext);
        mCompositeDisposable = new CompositeDisposable();
    }

    @Nullable
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FOLDER:
                return new FolderViewHolder(mLayoutInflater.inflate(R.layout.single_folder_layout, parent, false));
            case NOTES:
                return new NoteViewHolder(mLayoutInflater.inflate(R.layout.single_note_layout, parent, false));
            case HEADER:
                return new HeaderViewHolder(mLayoutInflater.inflate(R.layout.single_list_header, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int pos = mFolderList.size() + 2;
        int tempPOS = position - 1;
        switch (holder.getItemViewType()) {
            case FOLDER:
                bindFolderData(holder, tempPOS);
                break;
            case NOTES:
                pos = position - pos;
                bindNotesData(holder, pos);
                break;
            case HEADER:
                try {
                    StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                    layoutParams.setFullSpan(true);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                bindHeaderData(holder, position);
                break;
        }
    }

    /**
     * @param holder   HeaderViewHolder object
     * @param position Position of/in the list
     *                 <p>
     *                 this function binds the data for header view-holder type
     *                 it check if folder or note list is size is greater than 0
     *                 if yes than shows the appropriate headers otherwise
     *                 hides them
     */
    private void bindHeaderData(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        if (headerViewHolder.mItemHeader != null) {
            if (position == 0) {
                if (mFolderList.size() > 0) {
                    headerViewHolder.mItemHeader.setVisibility(View.VISIBLE);
                    headerViewHolder.mItemHeader.setText(mContext.getResources().getString(R.string.headingFolder));
                } else {
                    headerViewHolder.mItemHeader.setVisibility(View.GONE);
                }
            } else {
                if (mNotesList.size() > 0) {
                    headerViewHolder.mItemHeader.setVisibility(View.VISIBLE);
                    headerViewHolder.mItemHeader.setText(mContext.getResources().getString(R.string.headingNotes));
                } else {
                    headerViewHolder.mItemHeader.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * @param holder   FolderViewHolder object
     * @param position Position of/in the list
     *                 <p>
     *                 this function binds the data for FolderViewHolder type
     *                 it takes data from folder list and sets them on textview of
     *                 FolderViewHolder
     */
    private void bindFolderData(RecyclerView.ViewHolder holder, int position) {
        FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
        if (folderViewHolder.mFolderNameText != null) {
            folderViewHolder.mFolderNameText.setText(mFolderList.get(position).getFolderName());
            folderViewHolder.mFolderNameText.setCompoundDrawableTintList(stateList(mFolderList.get(position).getColor()));
        }
    }

    /**
     * @param holder   NoteViewHolder object
     * @param position Position of/in the list
     *                 <p>
     *                 this function binds the data for NoteViewHolder type
     *                 it takes data from Notes list and sets title and
     *                 note content on textviews and others values.
     */
    private void bindNotesData(@NonNull RecyclerView.ViewHolder holder, int position) {
        final NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        NoteEntity object;
        try {
            object = mFileUtil.getNote(mNotesList.get(position).getFileName());

            //TITLE
            if (noteViewHolder.mNoteTitle != null) {
                if (object.getTitle() != null && !object.getTitle().isEmpty()) {
                    noteViewHolder.mNoteTitle.setVisibility(View.VISIBLE);
                    noteViewHolder.mNoteTitle.setText(object.getTitle());
                    noteViewHolder.mNoteTitle.setTextColor(Color.parseColor(object.getColor()));
                } else {
                    noteViewHolder.mNoteTitle.setVisibility(View.GONE);
                }
            }

            //CONTENT
            if (noteViewHolder.mNoteContent != null) {
                if (object.getNoteContent() != null && !object.getNoteContent().isEmpty()) {
                    noteViewHolder.mNoteContent.setVisibility(View.VISIBLE);
                    noteViewHolder.mNoteContent.setText(object.getNoteContent());
                } else {
                    noteViewHolder.mNoteContent.setVisibility(View.GONE);
                }

            }

            //IMAGES
            if (noteViewHolder.mNoteImage != null) {
                if (object.getImageList() != null && object.getImageList().size() > 0) {
                    noteViewHolder.mNoteImage.setVisibility(View.VISIBLE);
                    mRequestManager.load(new File(mRootFolder, object.getImageList().get(0))).into(noteViewHolder.mNoteImage);
                } else {
                    noteViewHolder.mNoteImage.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return mNotesList != null && mFolderList != null ? mNotesList.size() + mFolderList.size() + 2 : 2;
    }

    //TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
    public void updateNotesList(@NonNull List<NoteEntity> noteList) {
        mNotesList = noteList;
        notifyDataSetChanged();
    }

    //TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
    public void updateFolderList(@NonNull List<FolderEntity> folderList) {
        mFolderList = folderList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == mFolderList.size() + 1) {
            return HEADER;
        } else if (position > 0 && position <= mFolderList.size()) {
            return FOLDER;
        } else if (position > mFolderList.size() + 1 && position < mNotesList.size()) {
            return NOTES;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() == target.getItemViewType()) {
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    //TODO
    @Override
    public void onItemMoved(int fromPosition, int toPosition, RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int startPos = -100;
        if (viewHolder.getItemViewType() == NOTES) {
            startPos = mFolderList.size() + 2;
        }
        if (viewHolder.getItemViewType() == FOLDER) {
            startPos = 1;
        }
        int fromPos = fromPosition - startPos;
        int toPos = toPosition - startPos;
    }

    //TODO
    @Override
    public void onItemDismiss(int position) {
        //notifyItemRemoved(position);
    }

    @NonNull
    private ColorStateList stateList(String colorString) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_pressed}
        };
        int color = Color.parseColor(colorString);
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    private void cleanUp() {
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        cleanUp();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.singleNoteTitle)
        TextView mNoteTitle;

        @Nullable
        @BindView(R.id.singleNoteContent)
        TextView mNoteContent;

        @Nullable
        @BindView(R.id.singleNoteImage)
        ImageView mNoteImage;

        NoteViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mCompositeDisposable.add(RxView.clicks(itemView).subscribe(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mNoteItemClickListener.onClick(getAdapterPosition(), getItemViewType());
                }
            }, throwable -> Timber.d(throwable.getMessage())));

            mCompositeDisposable.add(RxView.longClicks(itemView).subscribe(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mNoteItemClickListener.onLongClick(getAdapterPosition(), getItemViewType());
                }
            }, throwable -> Timber.d(throwable.getMessage())));
        }
    }


    class FolderViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.singleFolderName)
        TextView mFolderNameText;

        FolderViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mCompositeDisposable.add(RxView.clicks(itemView).subscribe(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mNoteItemClickListener.onClick(getAdapterPosition(), getItemViewType());
                }
            }));

            mCompositeDisposable.add(RxView.longClicks(itemView).subscribe(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    mNoteItemClickListener.onLongClick(getAdapterPosition(), getItemViewType());
                }
            }, throwable -> Timber.d(throwable.getMessage())));
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.itemHeader)
        TextView mItemHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}