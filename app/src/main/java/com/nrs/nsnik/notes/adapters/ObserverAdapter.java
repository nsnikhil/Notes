package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nrs.nsnik.notes.ContainerActivity;
import com.nrs.nsnik.notes.FileOperation;
import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderDataObserver;
import com.nrs.nsnik.notes.data.NoteDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.FolderCount;
import com.nrs.nsnik.notes.interfaces.ItemTouchListener;
import com.nrs.nsnik.notes.interfaces.NotesCount;
import com.nrs.nsnik.notes.interfaces.Observer;
import com.nrs.nsnik.notes.objects.NoteObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ObserverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Observer, ItemTouchListener {

    private static final String TAG = ObserverAdapter.class.getSimpleName();
    private Context mContext;
    private List<NoteObject> mNotesList;
    private List<String> mFolderList;
    private String mFolderName;
    private NotesCount mNotesCount;
    private FolderCount mFolderCount;
    private File mFolder;
    private static final String[] colorArray = {"#D32F2F", "#C2185B", "#7B1FA2", "#512DA8", "#303F9F", "#1976D2", "#0288D1",
            "#0097A7", "#00796B", "#388E3C", "#689F38", "#AFB42B", "#FBC02D", "#FFA000", "#F57C00", "#E64A19"};
    private Random r = new Random();
    private static final int NOTES = 0, FOLDER = 1,HEADER = 2;

    public ObserverAdapter(Context context, Uri noteUri, Uri folderUri, NotesCount notesCount, FolderCount folderCount
            , LoaderManager manager, String folderName) {
        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();
        mContext = context;
        mFolderName = folderName;
        mNotesCount = notesCount;
        mFolderCount = folderCount;
        mFolder =  mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
        NoteDataObserver noteDataObserver = new NoteDataObserver(mContext, noteUri, manager);
        noteDataObserver.add(this);
        FolderDataObserver folderDataObserver = new FolderDataObserver(mContext, folderUri, manager);
        folderDataObserver.add(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case FOLDER:
                return new FolderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_folder_layout, parent, false));
            case NOTES:
                return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_note_layout, parent, false));
            case HEADER:
                return new HeaderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_list_header,parent,false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int pos = mFolderList.size()+2;
        switch (holder.getItemViewType()) {
            case FOLDER:
                //bindFolderData(holder, position);
                break;
            case NOTES:
                pos = position - pos;
                //bindNotesData(holder, pos);
                --pos;
                break;
            case HEADER:
                //bindHeaderData(holder,position);
                break;
        }
    }

    private void bindHeaderData(RecyclerView.ViewHolder holder, int position){
        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
        if(position==0){
            headerViewHolder.mItemHeader.setText("Folders");
        } else {
            headerViewHolder.mItemHeader.setText("Notes");
        }
    }

    private void bindFolderData(RecyclerView.ViewHolder holder, int position) {
        FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
        folderViewHolder.mFolderName.setText(mFolderList.get(position));
    }

    private void bindNotesData(RecyclerView.ViewHolder holder, int position) {
        NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        NoteObject object = mNotesList.get(position);
        noteViewHolder.mNoteTitle.setText(object.getTitle());
        noteViewHolder.mNoteContent.setText(object.getNote());
        if (object.getImages().size() > 0) {
            setImage((NoteViewHolder) holder, object.getImages().get(position));
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

    private void setImage(final ObserverAdapter.NoteViewHolder holder, final String fileName){
        Single<Bitmap> singleObservable = Single.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return BitmapFactory.decodeFile(new File(mFolder,fileName).toString());
            }
        });
        singleObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Bitmap>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d(TAG, d.toString());
                    }

                    @Override
                    public void onSuccess(@NonNull Bitmap bitmap) {
                       holder.mNoteImage.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mNotesList.size() + mFolderList.size()+2;
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
    adds item from notes table to
     a list - @mNotesList
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

    private String justifyName(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
    }


    @Override
    public int getItemViewType(int position) {
        if(position==0||position==mFolderList.size()+1){
            return HEADER;
        }
        else if (position > 0 && position <= mFolderList.size()) {
            return FOLDER;
        }
        else if (position > mFolderList.size()+1 && position < mNotesList.size()) {
            return NOTES;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        /*if (viewHolder.getItemViewType() == FOLDER) {
            if(viewHolder.getItemViewType()==target.getItemViewType()) {
                shiftIds(fromPosition, toPosition, true);
            }
        }
        if (viewHolder.getItemViewType() == NOTES) {
            if(viewHolder.getItemViewType()==target.getItemViewType()) {
                shiftIds(fromPosition, toPosition, false);
            }
        }*/
        notifyItemMoved(fromPosition, toPosition);
    }

    private void shiftIds(int fromPosition, int toPosition, boolean isFolder) {
        if (isFolder) {
            //mFolderIds.set(fromPosition, toPosition);
            //mFolderIds.set(toPosition, fromPosition);
        } else {
            int tempFromPos = fromPosition - mFolderList.size();
            int tempToPos = toPosition - mFolderList.size();
            //int actFromPos = mNoteIds.get(tempFromPos);
            //int actTosPos = mNoteIds.get(tempToPos);
            //mNoteIds.set(tempFromPos, actTosPos);
            //mNoteIds.set(tempToPos, actFromPos);
        }
    }

    /*private void shiftItems(int fromPosition, int toPosition, Uri uri, boolean isFolder) {
        int tempOld = 0;
        int tempNew = 0;
        Uri fromUri, toUri;
        Uri newFrom, newTo;
        if (isFolder) {
            fromUri = Uri.withAppendedPath(uri, String.valueOf(mFolderIds.get(fromPosition)));
            toUri = Uri.withAppendedPath(uri, String.valueOf(mFolderIds.get(toPosition)));
        } else {
            int tempFromPos = fromPosition - mFolderList.size();
            int tempToPos = toPosition - mFolderList.size();
            fromUri = Uri.withAppendedPath(uri, String.valueOf(mNoteIds.get(tempFromPos)));
            toUri = Uri.withAppendedPath(uri, String.valueOf(mNoteIds.get(tempToPos)));
        }
        Cursor fromQuery = mContext.getContentResolver().query(fromUri, null, null, null, null);
        Cursor toQuery = mContext.getContentResolver().query(toUri, null, null, null, null);
        try {
            if (fromQuery != null && fromQuery.moveToFirst()) {
                if (isFolder) {
                    tempOld = fromQuery.getInt(fromQuery.getColumnIndex(TableNames.table2.mUid));
                } else {
                    tempOld = fromQuery.getInt(fromQuery.getColumnIndex(TableNames.table1.mUid));
                }
            }
            if (toQuery != null && toQuery.moveToFirst()) {
                if (isFolder) {
                    tempNew = toQuery.getInt(toQuery.getColumnIndex(TableNames.table2.mUid));
                } else {
                    tempNew = toQuery.getInt(toQuery.getColumnIndex(TableNames.table1.mUid));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fromQuery != null) {
                fromQuery.close();
            }
            if (toQuery != null) {
                toQuery.close();
            }
        }
        if (isFolder) {
            shiftInDatabase(fromUri, TableNames.table2.mUid, 9002);
            shiftInDatabase(toUri, TableNames.table2.mUid, 9003);

            newFrom = Uri.withAppendedPath(uri, String.valueOf(9002));
            newTo = Uri.withAppendedPath(uri, String.valueOf(9003));

            shiftInDatabase(newFrom, TableNames.table2.mUid, tempNew);
            shiftInDatabase(newTo, TableNames.table2.mUid, tempOld);
        } else {
            shiftInDatabase(fromUri, TableNames.table1.mUid, 9004);
            shiftInDatabase(toUri, TableNames.table1.mUid, 9005);

            newFrom = Uri.withAppendedPath(uri, String.valueOf(9004));
            newTo = Uri.withAppendedPath(uri, String.valueOf(9005));

            shiftInDatabase(newFrom, TableNames.table1.mUid, tempNew);
            shiftInDatabase(newTo, TableNames.table1.mUid, tempOld);
        }
    }*/

    private void shiftInDatabase(Uri uri, String uidKey, int newId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(uidKey, newId);
        mContext.getContentResolver().update(uri, contentValues, null, null);
    }

    @Override
    public void onItemDismiss(int position) {
        //notifyItemRemoved(position);
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
                @Override
                public void onClick(View v) {
                    int pos = mFolderList.size();
                    Intent intent = new Intent(mContext, NewNoteActivity.class);
                    //intent.setData(Uri.withAppendedPath(TableNames.mContentUri, String.valueOf(mNoteIds.get(getAdapterPosition() - pos))));
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "noteTitle");
                    mContext.startActivity(intent);
                }
            });
            mMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = mFolderList.size();
                }
            });
        }
    }

    private int getRandom() {
        int color = r.nextInt(colorArray.length);
        return Color.parseColor(colorArray[color]);
    }

    private ColorStateList stateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_pressed}
        };
        int color = getRandom();
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleFolderName)
        TextView mFolderName;
        @BindView(R.id.singleFolderMore)
        ImageButton mFolderMore;

        public FolderViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mFolderName.setCompoundDrawableTintList(stateList());
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ContainerActivity.class);
                    intent.putExtra(mContext.getResources().getString(R.string.intentFolderName), mFolderName.getText().toString());
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "noteFolder");
                    mContext.startActivity(intent);
                }
            });
            mFolderMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

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
                        if (isFolder) {
                            mContext.getContentResolver().delete(Uri.withAppendedPath(TableNames.mContentUri, folderName), null, null);
                        }
                        mContext.getContentResolver().delete(uri, null, null);
                    }
                });
        delete.create().show();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.itemHeader) TextView mItemHeader;
        @BindView(R.id.itemHeaderMore)ImageButton mHeaderMore;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            Point size = new Point();
            ((Activity)mContext).getWindowManager().getDefaultDisplay().getSize(size);
            int width = size.x;
            int height = size.y;
            itemView.setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.WRAP_CONTENT));
        }
    }

}
