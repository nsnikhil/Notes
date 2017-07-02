package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nrs.nsnik.notes.ContainerActivity;
import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderDataObserver;
import com.nrs.nsnik.notes.data.NoteDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.helpers.FileOperation;
import com.nrs.nsnik.notes.interfaces.FolderCount;
import com.nrs.nsnik.notes.interfaces.ItemTouchListener;
import com.nrs.nsnik.notes.interfaces.NotesCount;
import com.nrs.nsnik.notes.interfaces.Observer;
import com.nrs.nsnik.notes.objects.NoteObject;

import java.io.File;
import java.io.IOException;
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

public class ObserverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Observer, ItemTouchListener {

    private static final String TAG = ObserverAdapter.class.getSimpleName();
    private static final int NOTES = 0, FOLDER = 1, HEADER = 2;
    private Context mContext;
    private List<NoteObject> mNotesList;
    private List<String> mFolderList;
    private String mFolderName;
    private NotesCount mNotesCount;
    private FolderCount mFolderCount;
    private File mFolder;
    private FileOperation mFileOperations;
    private LayoutInflater mLayoutInflater;

    /*
    @param context      the context object
    @param noteUri      the uri to which queries the note table
    @param folderUri    the uri which queries the folder table
    @param notesCount   a interface which notifies about change in note list
    @param folderCount  a interface which notifies about change in folder list
    @param manager      the loader manager object used in data observers
    @param folderName   the name of the folder associated with the uri(note or folder)
     */
    public ObserverAdapter(Context context, Uri noteUri, Uri folderUri, NotesCount notesCount, FolderCount folderCount
            , LoaderManager manager, String folderName) {
        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mFolderName = folderName;
        mNotesCount = notesCount;
        mFolderCount = folderCount;
        mFolder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        mFileOperations = new FileOperation(mContext);
        NoteDataObserver noteDataObserver = new NoteDataObserver(mContext, noteUri, manager);
        noteDataObserver.add(this);
        FolderDataObserver folderDataObserver = new FolderDataObserver(mContext, folderUri, manager);
        folderDataObserver.add(this);
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
        folderViewHolder.mFolderNameText.setText(mFolderList.get(position));
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
        noteViewHolder.mNoteContent.setText(object.getNote());
        if (object.getImages().size() > 0) {
            ((NoteViewHolder) holder).mNoteImage.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(new File(mFolder, object.getImages().get(0)))
                    .into(((NoteViewHolder) holder).mNoteImage);
        } else {
            noteViewHolder.mNoteImage.setVisibility(View.GONE);
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

    @Override
    public int getItemCount() {
        return mNotesList.size() + mFolderList.size() + 2;
    }

    @Override
    public void updateItems(Cursor cursor) {
        if (cursor.getColumnIndex(TableNames.table1.mTitle) != -1) {
            makeNotesList(cursor);
        } else {
            makeFolderList(cursor);
        }
    }

    /*
    @param cursor   represents the cursor received after querying the noteUri

    this function add the content of each row of the cursor to the
    note list and clear the old list if any and also notifies the
    adapter about the change in data

    @TODO CHANGE NOTIFYDATASETCHANGE WITH DIFF UTIL
     */
    private void makeNotesList(Cursor cursor) {
        mNotesList.clear();
        while (cursor != null && cursor.moveToNext()) {
            NoteObject object = null;
            try {
                object = new FileOperation(mContext).readFile(cursor.getString(cursor.getColumnIndex(TableNames.table1.mFileName)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (object != null && object.getFolderName().equalsIgnoreCase(mFolderName)) {
                mNotesList.add(object);
            }
        }
        mNotesCount.getNotesCount(mNotesList.size());
        notifyDataSetChanged();
    }

    /*
   @param cursor   represents the cursor received after querying the FOLDERuRI

   this function add the content of each row of the cursor to the
   folder list and clear the old list if any and also notifies the
   adapter about the change in data

   @TODO CHANGE NOTIFYDATASETCHANGE WITH DIFF UTIL
    */
    private void makeFolderList(Cursor cursor) {
        mFolderList.clear();
        while (cursor != null && cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex(TableNames.table2.mParentFolderName)).equalsIgnoreCase(mFolderName)) {
                mFolderList.add(justifyName(cursor.getString(cursor.getColumnIndex(TableNames.table2.mFolderName))));
            }
        }
        mFolderCount.getFolderCount(mFolderList.size());
        notifyDataSetChanged();
    }

    /*
    @param name  takes a string as a argument
    @return String with first letter in upper case
     */
    private String justifyName(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
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
            getIdUri = Uri.withAppendedPath(TableNames.mContentUri, mFolderName);
        }

         /*
        if item is a folder than start position is
        1 (1st position is the folder header)
         getUri-->base uri is for folder
         */
        if (viewHolder.getItemViewType() == FOLDER) {
            startPos = 1;
            getIdUri = Uri.withAppendedPath(TableNames.mFolderContentUri, mFolderName);
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
                idList.add(mFileOperations.getId(getIdUri, tempFrom));
                --tempFrom;
            }
        } else {
            while (tempTo - tempFrom >= 0) {
                idList.add(mFileOperations.getId(getIdUri, tempFrom));
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
                mFileOperations.switchNoteId(mFileOperations.getId(getIdUri, fromPos), idList.get(i + 1));
            }
            if (viewHolder.getItemViewType() == FOLDER) {
                mFileOperations.switchFolderId(mFileOperations.getId(getIdUri, fromPos), idList.get(i + 1));
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
    @return new ColorSateList
     */
    private ColorStateList stateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_pressed}
        };
        int color = ContextCompat.getColor(mContext, R.color.colorAccentLight);
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
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
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popUpStar:
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
            }
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
                .setNegativeButton(mContext.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(mContext.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete(uri, isFolder, folderName);
                    }
                });
        delete.create().show();
    }

    /*
     @param uri             uri of the item upon which delete operation will be taken
     @param isFolder        check if uri corresponds to folder
     @param folderName      name of the folder
     */
    private void delete(Uri uri, boolean isFolder, String folderName) {
        Uri noteUri;
        if (isFolder) {
            noteUri = Uri.withAppendedPath(TableNames.mContentUri, folderName);
        } else {
            noteUri = uri;
        }
        try {
            mFileOperations.deleteFile(noteUri);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (isFolder) {
                mContext.getContentResolver().delete(Uri.withAppendedPath(TableNames.mContentUri, folderName), null, null);
            }
        }
        mContext.getContentResolver().delete(uri, null, null);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
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
        @BindView(R.id.singleNoteMore)
        ImageButton mMore;

        public NoteViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void onClick(View v) {
                    //check if position is valid
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        /*
                        get the id for the note at the particular
                        position and attach it to content uri and
                        pass it to new note activity
                         */
                        int startPos = mFolderList.size() + 2;
                        int currPos = getAdapterPosition() - startPos;
                        Intent intent = new Intent(mContext, NewNoteActivity.class);
                        intent.setData(Uri.withAppendedPath(TableNames.mContentUri,
                                String.valueOf(mFileOperations.getId(Uri.withAppendedPath(TableNames.mContentUri, mFolderName), currPos))));
                        Pair<View, String> p1 = Pair.create(itemView, "noteContainer");
                        Pair<View, String> p2 = Pair.create((View) mNoteTitle, "noteTitle");
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1, p2);
                        mContext.startActivity(intent, options.toBundle());
                    }
                }
            });
            mMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if position is valid
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        /*
                        calculate the id for the particular position and
                        use it create a uri that is passed to inflate menu
                        function
                         */
                        int startPos = mFolderList.size() + 2;
                        int currPos = getAdapterPosition() - startPos;
                        Uri uri = Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(mFileOperations.getId(Uri.withAppendedPath(TableNames.mContentUri, mFolderName), currPos)));
                        inflatePopUpMenu(mContext.getResources().getString(R.string.deleteSingleNoteWarning), false, "", uri, mMore);
                    }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mFolderNameText.setCompoundDrawableTintList(stateList());
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
            mFolderMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if position is valid
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                         /*
                        calculate the id for the particular position and
                        use it create a uri that is passed to inflate menu
                        function
                         */
                        int startPos = 1;
                        int currPos = getAdapterPosition() - startPos;
                        Uri baseUri = Uri.withAppendedPath(TableNames.mFolderContentUri, mFolderName);
                        int id = mFileOperations.getId(baseUri, currPos);
                        Uri uri = Uri.withAppendedPath(TableNames.mFolderContentUri, String.valueOf(id));
                        inflatePopUpMenu(mContext.getResources().getString(R.string.deleteSingleFolderWarning), true, mFolderNameText.getText().toString(), uri, mFolderMore);
                    }
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