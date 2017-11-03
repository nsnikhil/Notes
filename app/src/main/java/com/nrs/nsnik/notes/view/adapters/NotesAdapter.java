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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.util.FileUtil;
import com.nrs.nsnik.notes.util.events.FolderClickEvent;
import com.nrs.nsnik.notes.view.Henson;
import com.nrs.nsnik.notes.view.listeners.ItemTouchListener;
import com.nrs.nsnik.notes.viewmodel.FolderViewModel;
import com.nrs.nsnik.notes.viewmodel.NoteViewModel;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.time.LocalDateTime;
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
    private final NoteViewModel mNoteViewModel;
    private final FolderViewModel mFolderViewModel;
    private File mRootFolder;
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
                        NoteViewModel noteViewModel, FolderViewModel folderViewModel) {
        mContext = context;
        mNotesList = noteList;
        mFolderList = folderList;

        mRequestManager = ((MyApplication) mContext.getApplicationContext()).getRequestManager();
        mFileUtil = ((MyApplication) mContext.getApplicationContext()).getFileUtil();
        mRootFolder = mFileUtil.getRootFolder();

        mNoteViewModel = noteViewModel;
        mFolderViewModel = folderViewModel;

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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
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
            if (mNotesList.get(position).getFileName() != null) {
                Timber.d(mNotesList.get(position).getFileName());
            }
            object = mFileUtil.getNote(mNotesList.get(position).getFileName());

            //TITLE
            if (noteViewHolder.mNoteTitle != null) {
                noteViewHolder.mNoteTitle.setText(object.getTitle());
                noteViewHolder.mNoteTitle.setTextColor(Color.parseColor(object.getColor()));
            }

            //CONTENT
            if (noteViewHolder.mNoteContent != null) {
                noteViewHolder.mNoteContent.setText(object.getNoteContent());
            }

            //DATE
            if (noteViewHolder.mNoteDate != null) {
                noteViewHolder.mNoteDate.setText(formatDate(object.getDateModified()));
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

    @NonNull
    private String formatDate(LocalDateTime date) {
        LocalDateTime now = LocalDateTime.now();
        if (now.getYear() == date.getYear()) {
            if (now.getMonth() == date.getMonth()) {
                if (now.getDayOfMonth() == date.getDayOfMonth()) {
                    if (now.getHour() == date.getHour()) {
                        return (now.getMinute() - date.getMinute()) + " minutes ago";
                    } else {
                        return (now.getHour() - date.getHour()) + " hours ago";
                    }
                } else {
                    return (now.getDayOfMonth() - date.getDayOfMonth()) + " days ago";
                }
            } else {
                return (now.getMonth().getValue() - date.getMonth().getValue()) + " months ago";
            }
        } else {
            return (now.getYear() - date.getYear()) + " years ago";
        }
    }

    @Override
    public int getItemCount() {
        return mNotesList != null && mFolderList != null ? mNotesList.size() + mFolderList.size() + 2 : 2;
    }

    /**
     * TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
     */
    public void updateNotesList(@NonNull List<NoteEntity> noteList) {
        mNotesList = noteList;
    }

    /**
     * TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
     */
    public void updateFolderList(@NonNull List<FolderEntity> folderList) {
        mFolderList = folderList;
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

    @Override
    public void onItemMoved(int fromPosition, int toPosition, RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        /*
         *  After items are moved id for each
         */
        /*int startPos = -100;
        Uri getIdUri = null;

        /*
         * if item is a note than start position is
         * folder size + 2 (folder size + the two header)
         * getUri-->base uri is for note
         */
        /*if (viewHolder.getItemViewType() == NOTES) {
            startPos = mFolderList.size() + 2;
            getIdUri = Uri.withAppendedPath(TableNames.mContentUri, "parentFolderName/" + mFolderName);
        }

         /*
          * if item is a folder than start position is
          * 1 (1st position is the folder header)
          * getUri-->base uri is for folder
          */
        /*if (viewHolder.getItemViewType() == FOLDER) {
            startPos = 1;
            getIdUri = Uri.withAppendedPath(TableNames.mFolderContentUri, "parentFolderName/" + mFolderName);
        }

        /*
        if an item has been move for a certain range than
        calculate the id of each item in range and
        store them in a list

        @fromPos    actual starting position of that item view type
                    fromPosition    = adapter Position
                    startPos        = position from which this view type starts

        @tpPos      position to which the item was scrolled
                    toPosition      = Position to which the item was scrolled
                    startPos        = position from which this view type starts
         */
        /*List<Integer> idList = new ArrayList<>();
        int fromPos = fromPosition - startPos;
        int toPos = toPosition - startPos;
        int tempFrom = fromPos;
        /*
        add the id from starting position to the
        end position and if movement was from
        bottom to top decrement the tempFrom else
        increment it
         */
        /*if (tempFrom > toPos) {
            while (tempFrom - toPos >= 0) {
                if (getIdUri != null) {
                    idList.add(mDatabaseOperations.getId(getIdUri, tempFrom));
                    --tempFrom;
                }
            }
        } else {
            while (toPos - tempFrom >= 0) {
                if (getIdUri != null) {
                    idList.add(mDatabaseOperations.getId(getIdUri, tempFrom));
                }
                ++tempFrom;
            }
        }

        /*
        swap id of each item from starting position with
        the id of the previous or next item depending upon
        if item was going upwards or downwards
         */
        /*for (int i = 0; i < idList.size() - 1; i++) {
            if (viewHolder.getItemViewType() == NOTES) {
                if (getIdUri != null) {
                    mDatabaseOperations.switchNoteId(mDatabaseOperations.getId(getIdUri, fromPos), idList.get(i + 1));
                }
            }
            if (viewHolder.getItemViewType() == FOLDER) {
                if (getIdUri != null) {
                    mDatabaseOperations.switchFolderId(mDatabaseOperations.getId(getIdUri, fromPos), idList.get(i + 1));
                }
            }
            /*
            if starting position is greater i.e. going
            from bottom to top decrement the value of
            starting position else increment
             */
            /*if (fromPos > toPos) {
                --fromPos;
            } else {
                ++fromPos;
            }
        }*/
    }

    @Override
    public void onItemDismiss(int position) {
        //notifyItemRemoved(position);
    }


    private void inflatePopUpMenu(final String message, final boolean isFolder,
                                  FolderEntity folderEntity, NoteEntity noteEntity, @NonNull View itemView) {
        PopupMenu menu = new PopupMenu(mContext, itemView, Gravity.START);
        menu.inflate(R.menu.pop_up_menu);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popUpStar:
                    Toast.makeText(mContext, "TO-DO", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.popUpLock:
                    Toast.makeText(mContext, "TO-DO", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.popUpEdit:
                    Toast.makeText(mContext, "TO-DO", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.popUpMove:
                    Toast.makeText(mContext, "TO-DO", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.popUpShare:
                    Toast.makeText(mContext, "TO-DO", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.popUpDelete:
                    makeDeleteDialog(message, folderEntity, noteEntity, isFolder);
                    break;
            }
            return false;
        });
        menu.show();
    }

    private void makeDeleteDialog(String message, FolderEntity folderEntity, NoteEntity noteEntity, final boolean isFolder) {
        AlertDialog.Builder delete = new AlertDialog.Builder(mContext);
        delete.setTitle(mContext.getResources().getString(R.string.warning))
                .setMessage(message)
                .setNegativeButton(mContext.getResources().getString(R.string.no), (dialogInterface, i) -> {
                })
                .setPositiveButton(mContext.getResources().getString(R.string.yes), (dialogInterface, i) -> delete(isFolder, folderEntity, noteEntity));
        delete.create().show();
    }


    private void delete(boolean isFolder, FolderEntity folderEntity, NoteEntity noteEntity) {
        if (isFolder) {
            mFolderViewModel.deleteFolder(folderEntity);
        } else {
            mNoteViewModel.deleteNote(noteEntity);
        }
    }

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
        @BindView(R.id.singleNoteDate)
        TextView mNoteDate;

        @Nullable
        @BindView(R.id.singleNoteImage)
        ImageView mNoteImage;

        @Nullable
        @BindView(R.id.singleNoteMore)
        ImageButton mMore;

        NoteViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mCompositeDisposable.add(RxView.clicks(itemView).subscribe(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    int startPos = mFolderList.size() + 2;
                    int currPos = getAdapterPosition() - startPos;
                    NoteEntity noteEntity = mFileUtil.getNote(mNotesList.get(currPos).getFileName());
                    Intent noteIntent = Henson.with(mContext)
                            .gotoNewNoteActivity()
                            .mNoteId(noteEntity.getUid())
                            .mNoteEntity(noteEntity)
                            .build();
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "noteContainer");
                    mContext.startActivity(noteIntent, options.toBundle());

                }
            }, throwable -> Timber.d(throwable.getMessage())));
            if (mMore != null) {
                mCompositeDisposable.add(RxView.clicks(mMore).subscribe(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {

                        int startPos = mFolderList.size() + 2;
                        int currPos = getAdapterPosition() - startPos;

                        inflatePopUpMenu(mContext.getResources().getString(R.string.deleteSingleNoteWarning),
                                false, null, mNotesList.get(currPos), mMore);

                    }
                }, throwable -> Timber.d(throwable.getMessage())));
            }
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.singleFolderName)
        TextView mFolderNameText;
        @Nullable
        @BindView(R.id.singleFolderMore)
        ImageButton mFolderMore;

        FolderViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mFolderNameText != null) {
                mCompositeDisposable.add(RxView.clicks(itemView).subscribe(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        int startPos = 1;
                        int currPos = getAdapterPosition() - startPos;
                        EventBus.getDefault().post(new FolderClickEvent(mFolderList.get(currPos).getFolderName()));
                    }
                }));
            }
            if (mFolderMore != null && mFolderNameText != null) {
                mCompositeDisposable.add(RxView.clicks(mFolderMore).subscribe(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        int startPos = 1;
                        int currPos = getAdapterPosition() - startPos;
                        inflatePopUpMenu(mContext.getResources().getString(R.string.deleteSingleFolderWarning),
                                true, mFolderList.get(currPos), null, mFolderMore);
                    }
                }));
            }
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