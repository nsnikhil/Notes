package com.nrs.nsnik.notes.view.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.data.NoteEntity;
import com.nrs.nsnik.notes.util.RvItemTouchHelper;
import com.nrs.nsnik.notes.view.adapters.NotesAdapter;
import com.nrs.nsnik.notes.viewmodel.FolderViewModel;
import com.nrs.nsnik.notes.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ListFragment extends Fragment {

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

        mNoteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel.class);

        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();


        //Setting up recycler view

        mNotesAdapter = new NotesAdapter(getActivity(), mNotesList, mFolderList, mNoteViewModel, mFolderViewModel);

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
        mNotesAdapter.notifyDataSetChanged();
        setEmpty();
    }

    private void swapNotes(List<NoteEntity> noteEntityList) {
        if (noteEntityList == null) return;
        mNotesList = noteEntityList;
        mNotesAdapter.updateNotesList(mNotesList);
        mNotesAdapter.notifyDataSetChanged();
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
}
