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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
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
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.FolderObject;
import com.nrs.nsnik.notes.model.objects.NoteObject;
import com.nrs.nsnik.notes.util.DatabaseOperations;
import com.nrs.nsnik.notes.util.FileOperation;
import com.nrs.nsnik.notes.util.interfaces.ItemTouchListener;
import com.nrs.nsnik.notes.view.ContainerActivity;
import com.nrs.nsnik.notes.view.MyApplication;
import com.nrs.nsnik.notes.view.NewNoteActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
This adapter takes in a uri queries the uri and
registers for change in uri i.e. if any items in
that uri changes the adapter is notified and then
adapters clear the old list re draws the entire list
with the new data set
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchListener {

    private static final int NOTES = 0, FOLDER = 1, HEADER = 2;
    private final Context mContext;
    private final String mFolderName;
    private final File mFolder;
    private final LayoutInflater mLayoutInflater;
    private final RequestManager mRequestManager;
    private final FileOperation mFileOperation;
    private final DatabaseOperations mDatabaseOperations;
    private List<NoteObject> mNotesList;
    private List<FolderObject> mFolderList;


    /*
    TODO ENABLE SHOW TRANSITIONING OF LAYOUT CHANGES
     */

    /*
    @param context      the context object
    @param noteUri      the uri to which queries the note table
    @param folderUri    the uri which queries the folder table
    @param notesCount   a interface which notifies about change in note list
    @param folderCount  a interface which notifies about change in folder list
    @param manager      the loader manager object used in data observers
    @param folderName   the name of the folder associated with the uri(note or folder)
     */
    public NotesAdapter(Context context, List<NoteObject> noteList, List<FolderObject> folderList, String folderName) {
        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();
        mNotesList.addAll(noteList);
        mFolderList.addAll(folderList);
        mContext = context;


        mFileOperation = ((MyApplication) mContext.getApplicationContext()).getFileOperations();
        mDatabaseOperations = ((MyApplication) mContext.getApplicationContext()).getDatabaseOperations();
        mFolder = ((MyApplication) mContext.getApplicationContext()).getRootFolder();
        mRequestManager = ((MyApplication) mContext.getApplicationContext()).getGlideComponent().getRequestManager();


        mLayoutInflater = LayoutInflater.from(mContext);
        mFolderName = folderName;
    }

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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
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

    /*
        @param holder       HeaderViewHolder object
        @param position     Position of/in the list

        this function binds the data for header view-holder type
        it check if folder or note list is size is greater than 0
        if yes than shows the appropriate headers otherwise
        hides them
         */
    private void bindHeaderData(RecyclerView.ViewHolder holder, int position) {
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
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

    /*
        @param holder       FolderViewHolder object
        @param position     Position of/in the list

        this function binds the data for FolderViewHolder type
        it takes data from folder list and sets them on textview of
        FolderViewHolder
         */
    private void bindFolderData(RecyclerView.ViewHolder holder, int position) {
        FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
        folderViewHolder.mFolderNameText.setText(mFolderList.get(position).getmFolderName());
        folderViewHolder.mFolderNameText.setCompoundDrawableTintList(stateList(mFolderList.get(position).getmFolderColor()));
    }

    /*
   @param holder       NoteViewHolder object
   @param position     Position of/in the list

   this function binds the data for NoteViewHolder type
   it takes data from Notes list and sets title and
   note content on textviews and others values.
    */
    private void bindNotesData(RecyclerView.ViewHolder holder, int position) {
        final NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        NoteObject object = mNotesList.get(position);
        noteViewHolder.mNoteTitle.setText(object.getTitle());
        noteViewHolder.mNoteTitle.setTextColor(Color.parseColor(object.getmColor()));
        noteViewHolder.mNoteContent.setText(object.getNote());
        noteViewHolder.mNoteDate.setText(mFileOperation.formatDate((object.getmTime())));
        if (object.getImages().size() > 0) {
            ((NoteViewHolder) holder).mNoteImage.setVisibility(View.VISIBLE);
            mRequestManager.load(new File(mFolder, object.getImages().get(0))).into(((NoteViewHolder) holder).mNoteImage);
        } else {
            noteViewHolder.mNoteImage.setVisibility(View.GONE);
        }
        if (object.getAudioLocations().size() > 0) {
            noteViewHolder.mAudIndicator.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.mAudIndicator.setVisibility(View.GONE);
        }
        if (object.getmCheckList().size() > 0) {
            noteViewHolder.mChkLstIndicator.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.mChkLstIndicator.setVisibility(View.GONE);
        }
        if (object.getReminder() != 0) {
            noteViewHolder.mRemIndicator.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.mRemIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mNotesList.size() + mFolderList.size() + 2;
    }

    /*
    @TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
     */
    public void updateNotesList(List<NoteObject> noteList) {
        mNotesList.clear();
        mNotesList.addAll(noteList);
        notifyDataSetChanged();
    }

    /*
    @TODO CHANGE NOTIFY-DATA-SET-CHANGE WITH DIFF UTIL
     */
    public void updateFolderList(List<FolderObject> folderList) {
        mFolderList.clear();
        mFolderList.addAll(folderList);
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
    public void onItemMove(int fromPosition, int toPosition, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() == target.getItemViewType()) {
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        /*
        After items are moved id for each
         */
        int startPos = -100;
        Uri getIdUri = null;

        /*
        if item is a note than start position is
        folder size + 2 (folder size + the two header)
        getUri-->base uri is for note
         */
        if (viewHolder.getItemViewType() == NOTES) {
            startPos = mFolderList.size() + 2;
            getIdUri = Uri.withAppendedPath(TableNames.mContentUri, "parentFolderName/" + mFolderName);
        }

         /*
        if item is a folder than start position is
        1 (1st position is the folder header)
         getUri-->base uri is for folder
         */
        if (viewHolder.getItemViewType() == FOLDER) {
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
        List<Integer> idList = new ArrayList<>();
        int fromPos = fromPosition - startPos;
        int toPos = toPosition - startPos;
        int tempFrom = fromPos;
        int tempTo = toPos;
        /*
        add the id from starting position to the
        end position and if movement was from
        bottom to top decrement the tempFrom else
        increment it
         */
        if (tempFrom > tempTo) {
            while (tempFrom - tempTo >= 0) {
                idList.add(mDatabaseOperations.getId(getIdUri, tempFrom));
                --tempFrom;
            }
        } else {
            while (tempTo - tempFrom >= 0) {
                idList.add(mDatabaseOperations.getId(getIdUri, tempFrom));
                ++tempFrom;
            }
        }

        /*
        swap id of each item from starting position with
        the id of the previous or next item depending upon
        if item was going upwards or downwards
         */
        for (int i = 0; i < idList.size() - 1; i++) {
            if (viewHolder.getItemViewType() == NOTES) {
                mDatabaseOperations.switchNoteId(mDatabaseOperations.getId(getIdUri, fromPos), idList.get(i + 1));
            }
            if (viewHolder.getItemViewType() == FOLDER) {
                mDatabaseOperations.switchFolderId(mDatabaseOperations.getId(getIdUri, fromPos), idList.get(i + 1));
            }
            /*
            if starting position is greater i.e. going
            from bottom to top decrement the value of
            starting position else increment
             */
            if (fromPos > toPos) {
                --fromPos;
            } else {
                ++fromPos;
            }
        }
    }

    @Override
    public void onItemDismiss(int position) {
        //notifyItemRemoved(position);
    }

    /*
     @param message         message to be displayed in dialog box while deleting
     @param isFolder        check if folder inflated the menu or not
     @param folderName      name of the folder that inflated the menu
     @param uri             uri of the item upon which actions will be taken
     @param itemView        view to which the particular menu will be attached

     @TODO POP UP STAR THE IETM
     @TODO POP UP MENU SHARE THE ITEM
     */
    private void inflatePopUpMenu(final String message, final boolean isFolder, final String folderName, final Uri uri, View itemView) {
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
                    makeDeleteDialog(message, uri, isFolder, folderName);
                    break;
            }
            return false;
        });
        menu.show();
    }

    /*
     @param message         message to be displayed in dialog box while deleting
     @param uri             uri of the item upon which delete operation will be taken
     @param isFolder        check if uri corresponds to folder
     @param folderName      name of the folder
     */
    private void makeDeleteDialog(String message, final Uri uri, final boolean isFolder, final String folderName) {
        AlertDialog.Builder delete = new AlertDialog.Builder(mContext);
        delete.setTitle(mContext.getResources().getString(R.string.warning))
                .setMessage(message)
                .setNegativeButton(mContext.getResources().getString(R.string.no), (dialogInterface, i) -> {

                })
                .setPositiveButton(mContext.getResources().getString(R.string.yes), (dialogInterface, i) -> delete(uri, isFolder, folderName));
        delete.create().show();
    }

    /*
     @param uri             uri of the item upon which delete operation will be taken
     @param isFolder        check if uri corresponds to folder
     @param folderName      name of the folder
     */
    private void delete(Uri uri, boolean isFolder, String folderName) {
        if (isFolder) {
            mFileOperation.deleteFolder(uri, folderName);
        } else {
            mFileOperation.deleteNote(uri);
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

    class NoteViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleNoteTitle)
        TextView mNoteTitle;
        @BindView(R.id.singleNoteContent)
        TextView mNoteContent;
        @BindView(R.id.singleNoteDate)
        TextView mNoteDate;
        @BindView(R.id.singleNoteImage)
        ImageView mNoteImage;
        @BindView(R.id.singleNoteReminder)
        ImageView mRemIndicator;
        @BindView(R.id.singleNoteAudio)
        ImageView mAudIndicator;
        @BindView(R.id.singleNoteCheck)
        ImageView mChkLstIndicator;
        @BindView(R.id.singleNoteMore)
        ImageButton mMore;
        @BindView(R.id.singleNoteCard)
        CardView mNoteCard;

        public NoteViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                //check if position is valid
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    /*
                    get the id for the note at the particular
                    position and pass it along with the serialized
                    note object to the new note activity
                     */
                    int startPos = mFolderList.size() + 2;
                    int currPos = getAdapterPosition() - startPos;
                    int noteId = mDatabaseOperations.getId(Uri.withAppendedPath(TableNames.mContentUri, "parentFolderName/" + mFolderName), currPos);
                    Intent intent = new Intent(mContext, NewNoteActivity.class);

                    Bundle noteArgs = new Bundle();
                    noteArgs.putSerializable(mContext.getResources().getString(R.string.bundleNoteSerialObject), mNotesList.get(currPos));
                    noteArgs.putInt(mContext.getResources().getString(R.string.bundleNoteSerialId), noteId);

                    intent.putExtras(noteArgs);

                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "noteContainer");
                    mContext.startActivity(intent, options.toBundle());
                }
            });
            mMore.setOnClickListener(v -> {
                //check if position is valid
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    /*
                    calculate the id for the particular position and
                    use it create a uri that is passed to inflate menu
                    function
                     */
                    int startPos = mFolderList.size() + 2;
                    int currPos = getAdapterPosition() - startPos;
                    Uri baseUri = Uri.withAppendedPath(TableNames.mContentUri, "parentFolderName/" + mFolderName);
                    int id = mDatabaseOperations.getId(baseUri, currPos);
                    Uri uri = Uri.withAppendedPath(TableNames.mContentUri, "noteId/" + id);
                    inflatePopUpMenu(mContext.getResources().getString(R.string.deleteSingleNoteWarning), false, "", uri, mMore);
                }
            });
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleFolderName)
        TextView mFolderNameText;
        @BindView(R.id.singleFolderMore)
        ImageButton mFolderMore;

        public FolderViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                //check if position is valid
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    /*
                      get the id for the folder at the particular
                      position and attach it to folder content uri and
                      pass it to container activity
                    */
                    Intent intent = new Intent(mContext, ContainerActivity.class);
                    intent.putExtra(mContext.getResources().getString(R.string.intentFolderName), mFolderNameText.getText().toString());
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "noteFolder");
                    mContext.startActivity(intent, options.toBundle());
                }
            });
            mFolderMore.setOnClickListener(v -> {
                //check if position is valid
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                     /*
                    calculate the id for the particular position and
                    use it create a uri that is passed to inflate menu
                    function
                     */
                    int startPos = 1;
                    int currPos = getAdapterPosition() - startPos;
                    Uri baseUri = Uri.withAppendedPath(TableNames.mFolderContentUri, "parentFolderName/" + mFolderName);
                    int id = mDatabaseOperations.getId(baseUri, currPos);
                    Uri uri = Uri.withAppendedPath(TableNames.mFolderContentUri, "folderId/" + id);
                    inflatePopUpMenu(mContext.getResources().getString(R.string.deleteSingleFolderWarning), true, mFolderNameText.getText().toString(), uri, mFolderMore);
                }
            });
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemHeader)
        TextView mItemHeader;
        @BindView(R.id.itemHeaderMore)
        ImageButton mHeaderMore;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}