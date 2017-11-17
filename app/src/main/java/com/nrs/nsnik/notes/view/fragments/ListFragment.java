package com.nrs.nsnik.notes.view.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.util.FileUtil;
import com.nrs.nsnik.notes.util.RvItemTouchHelper;
import com.nrs.nsnik.notes.util.events.FolderClickEvent;
import com.nrs.nsnik.notes.view.Henson;
import com.nrs.nsnik.notes.view.adapters.NotesAdapter;
import com.nrs.nsnik.notes.view.listeners.NoteItemClickListener;
import com.nrs.nsnik.notes.viewmodel.FolderViewModel;
import com.nrs.nsnik.notes.viewmodel.NoteViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ListFragment extends Fragment implements NoteItemClickListener {

    @BindView(R.id.commonList)
    RecyclerView mList;
    @BindView(R.id.commonListSwipe)
    SwipeRefreshLayout mRefresh;
    @BindView(R.id.emptyState)
    TextView mEmptyState;

    private Unbinder mUnbinder;

    private NoteViewModel mNoteViewModel;
    private FolderViewModel mFolderViewModel;

    private List<NoteEntity> mNotesList;
    private List<FolderEntity> mFolderList;

    private String mFolderName;
    private NotesAdapter mNotesAdapter;
    private FileUtil mFileUtil;

    private boolean mInEditorMode;
    private List<Integer> mSelectedNoteId, mSelectedFolderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_layout, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        return v;
    }

    private void initialize() {

        if (getActivity() != null && getArguments() != null) {
            mFolderName = getArguments().getString(getActivity().getResources().getString(R.string.bundleListFragmentFolderName));
        }

        if (getActivity() != null) {
            mFileUtil = ((MyApplication) getActivity().getApplication()).getFileUtil();
        }

        mSelectedNoteId = new ArrayList<>();
        mSelectedFolderId = new ArrayList<>();

        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel.class);

        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();


        //Setting up recycler view

        mNotesAdapter = new NotesAdapter(getActivity(), mNotesList, mFolderList, this);

        mList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        mList.setHasFixedSize(true);

        mList.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper touchHelper = new ItemTouchHelper(new RvItemTouchHelper(mNotesAdapter));
        touchHelper.attachToRecyclerView(mList);

        mList.setAdapter(mNotesAdapter);

        setViewModel();

    }

    private void setViewModel() {
        mNoteViewModel.getNoteByFolderName(mFolderName).observe(this, this::swapNotes);

        mFolderViewModel.getFolderByParent(mFolderName).observe(this, this::swapFolder);
    }

    private void swapFolder(List<FolderEntity> folderEntityList) {
        if (folderEntityList == null) return;
        mFolderList = folderEntityList;
        mNotesAdapter.updateFolderList(folderEntityList);
        setEmpty();
    }

    private void swapNotes(List<NoteEntity> noteEntityList) {
        if (noteEntityList == null) return;
        mNotesList = noteEntityList;
        mNotesAdapter.updateNotesList(mNotesList);
        setEmpty();
    }

    private void setEmpty() {
        if (mNotesList.size() == 0 && mFolderList.size() == 0) {
            mEmptyState.setVisibility(View.VISIBLE);
        } else {
            mEmptyState.setVisibility(View.GONE);
        }
    }

    private void cleanUp() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    private void openFolder(int position) {
        if (position != RecyclerView.NO_POSITION) {
            int startPos = 1;
            int currPos = position - startPos;
            EventBus.getDefault().post(new FolderClickEvent(mFolderList.get(currPos).getFolderName()));
        }
    }

    private void openNote(int position) throws Exception {
        if (getActivity() != null) {

            int startPos = mFolderList.size() + 2;
            int currPos = position - startPos;
            NoteEntity noteEntity = mFileUtil.getNote(mNotesList.get(currPos).getFileName());

            Intent noteIntent = Henson.with(getActivity())
                    .gotoNewNoteActivity()
                    .mNoteId(noteEntity.getUid())
                    .mNoteEntity(noteEntity)
                    .build();

            getActivity().startActivity(noteIntent);
        }
    }

    private void makeDeleteDialog(String message, FolderEntity folderEntity, NoteEntity noteEntity, final boolean isFolder) {
        if (getActivity() != null) {
            AlertDialog.Builder delete = new AlertDialog.Builder(getActivity());
            delete.setTitle(getActivity().getResources().getString(R.string.warning))
                    .setMessage(message)
                    .setNegativeButton(getActivity().getResources().getString(R.string.no), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getActivity().getResources().getString(R.string.yes), (dialogInterface, i) -> delete(isFolder, folderEntity, noteEntity));
            delete.create().show();
        }
    }


    private void delete(boolean isFolder, FolderEntity folderEntity, NoteEntity noteEntity) {
        if (isFolder) {
            mFolderViewModel.deleteFolder(folderEntity);
        } else {
            mNoteViewModel.deleteNote(noteEntity);
        }
    }

    @Override
    public void onClick(int position, int itemViewType) {
        switch (itemViewType) {
            case 0:
                try {
                    openNote(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                openFolder(position);
                break;
        }
    }

    @Override
    public void onLongClick(int position, int itemViewType) {
        switch (itemViewType) {
            case 0:
                if (!mInEditorMode) {

                }
                break;
            case 1:
                if (!mInEditorMode) {

                }
                break;
        }
    }
}
