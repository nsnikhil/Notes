/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.NewNoteActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.NotesAdapter;
import com.nrs.nsnik.notes.data.FolderDataObserver;
import com.nrs.nsnik.notes.data.NoteDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.fragments.dialogFragments.ColorPickerDialogFragment;
import com.nrs.nsnik.notes.helpers.FileOperation;
import com.nrs.nsnik.notes.helpers.RvItemTouchHelper;
import com.nrs.nsnik.notes.interfaces.NoteObserver;
import com.nrs.nsnik.notes.interfaces.OnColorSelectedListener;
import com.nrs.nsnik.notes.objects.FolderObject;
import com.nrs.nsnik.notes.objects.NoteObject;
import com.squareup.leakcanary.RefWatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/*
this fragment passes the uri to the adapter upon
which adapter queries and makes a list to display
 */

public class HomeFragment extends Fragment implements NoteObserver, OnColorSelectedListener {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final int GET_COLOR_REQUEST_CODE = 207;

    @BindView(R.id.commonList)
    RecyclerView mList;
    @BindView(R.id.homeEmptyState)
    TextView mEmpty;
    @BindView(R.id.fabAdd)
    FloatingActionButton mAddSpinner;
    @BindView(R.id.fabAddNote)
    FloatingActionButton mAddNote;
    @BindView(R.id.fabAddFolder)
    FloatingActionButton mAddFolder;
    @BindView(R.id.fabNoteContainer)
    LinearLayout mNoteContainer;
    @BindView(R.id.fabFolderContainer)
    LinearLayout mFolderContainer;
    @BindView(R.id.commonListSwipe)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.homeAdView)
    AdView mAdView;
    @BindView(R.id.homeContainer)
    CoordinatorLayout mHomeContainer;

    private String mFolderName = "nofolder";
    private String mColor;
    private ImageView mColorView;

    private Unbinder mUnbinder;
    private List<NoteObject> mNotesList;
    private List<FolderObject> mFolderList;
    private NotesAdapter mNotesAdapter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        MobileAds.initialize(getActivity(), getActivity().getResources().getString(R.string.adAdMobId));
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        return v;
    }

    /*
    if the fragment if attached to activity which was
    launch via intent then get the folder name
    and append to base content uri
     */
    private void getArgs() {
        if (getArguments() != null) {
            mFolderName = getArguments().getString(getActivity().getResources().getString(R.string.homefldnm));
        }
    }


    private void initialize() {
        getArgs();
        mNotesList = new ArrayList<>();
        mFolderList = new ArrayList<>();

        //Setting up recycler view
        mList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mNotesAdapter = new NotesAdapter(getActivity(), mNotesList, mFolderList, mFolderName);
        mList.setHasFixedSize(true);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        mList.setItemAnimator(itemAnimator);
        mList.setAdapter(mNotesAdapter);
        ItemTouchHelper.Callback callback = new RvItemTouchHelper(mNotesAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mList);

        //setting up observer
        String noteQuery = "parentFolderName/" + mFolderName;
        NoteDataObserver noteDataObserver = new NoteDataObserver(getActivity(), Uri.withAppendedPath(TableNames.mContentUri, noteQuery), getLoaderManager());
        noteDataObserver.add(this);

        String folderQuery = "parentFolderName/" + mFolderName;
        FolderDataObserver folderDataObserver = new FolderDataObserver(getActivity(), Uri.withAppendedPath(TableNames.mFolderContentUri, folderQuery), getLoaderManager());
        folderDataObserver.add(this);


        /*
        if the build is not debug
        enable ads
         */
        if (!BuildConfig.DEBUG) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void listeners() {
        mAddSpinner.setOnClickListener(v -> {
            if (mNoteContainer.getVisibility() == View.INVISIBLE || mFolderContainer.getVisibility() == View.INVISIBLE) {
                reveal();
            } else {
                disappear();
            }
        });
        mAddNote.setOnClickListener(v -> {
            disappear();
            Intent newNote = new Intent(getActivity(), NewNoteActivity.class);
            newNote.putExtra(getActivity().getResources().getString(R.string.newnotefolderbundle), mFolderName);
            startActivity(newNote);
        });
        mAddFolder.setOnClickListener(v -> {
            disappear();
            createFolderDialog();
        });
        mSwipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> mSwipeRefresh.setRefreshing(false), 1000));
    }

    /*
    if the add notes and folder fab is not visible
    the perform animation and then make them visible
     */
    private void reveal() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 135, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(50);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            Animation scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.jump_from_down);
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                scaleUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mFolderContainer.setVisibility(View.VISIBLE);
                        mNoteContainer.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mFolderContainer.startAnimation(scaleUp);
                mNoteContainer.startAnimation(scaleUp);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mAddSpinner.startAnimation(rotateAnimation);
    }

    /*
    if the add notes and folder fab is visible
    the perform animation and then make them in-visible
     */
    private void disappear() {
        RotateAnimation rotateAnimation = new RotateAnimation(135, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(50);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            Animation scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.jump_to_down);
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                scaleDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mFolderContainer.setVisibility(View.INVISIBLE);
                        mNoteContainer.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mFolderContainer.startAnimation(scaleDown);
                mNoteContainer.startAnimation(scaleDown);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mAddSpinner.startAnimation(rotateAnimation);
    }

    /*
    create a alert dialog with custom view
    to take a name from field
     */
    private void createFolderDialog() {
        AlertDialog.Builder newFolder = new AlertDialog.Builder(getActivity());
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_folder_dialog, null);
        newFolder.setView(v);
        final EditText editText = v.findViewById(R.id.dialogFolderName);
        mColorView = v.findViewById(R.id.dialogFolderColor);
        editText.requestFocus();
        mColor = null;
        mColorView.setOnClickListener(view -> {
            ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
            pickerDialogFragment.setTargetFragment(this, 129);
            pickerDialogFragment.show(getFragmentManager(), "color");
        });
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        newFolder.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0));
        newFolder.setPositiveButton(getResources().getString(R.string.create), (dialogInterface, i) -> {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            createFolder(editText.getText().toString());
        });
        newFolder.create().show();
    }

    /*
    @param name     create a folder of name = name
                    by adding the entry to database
     */
    private void createFolder(String name) {
        ContentValues cv = new ContentValues();
        cv.put(TableNames.table2.mFolderName, name);
        Calendar c = Calendar.getInstance();
        cv.put(TableNames.table2.mFolderId, c.getTimeInMillis() + name);
        cv.put(TableNames.table2.mParentFolderName, mFolderName);
        if (mColor != null) {
            cv.put(TableNames.table2.mColor, mColor);
        } else {
            cv.put(TableNames.table2.mColor, "#333333");
        }
        Uri u = getActivity().getContentResolver().insert(TableNames.mFolderContentUri, cv);
        if (u != null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.newfoldercreated), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.newfoldernotcreated), Toast.LENGTH_SHORT).show();
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
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(getActivity());
            refWatcher.watch(this);
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = super.onCreateAnimation(transit, enter, nextAnim);
        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        }
        if (animation != null) {
            if (getView() != null) {
                getView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (getView() != null) {
                        getView().setLayerType(View.LAYER_TYPE_NONE, null);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        return animation;
    }

    @Override
    public void updateItems(Cursor cursor) {
        if (cursor.getColumnIndex(TableNames.table1.mTitle) != -1) {
            makeNotesList(cursor);
        } else {
            makeFolderList(cursor);
        }
    }

    private void setEmpty() {
        if (mFolderList.size() <= 0 && mNotesList.size() <= 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }

    /*
    @param cursor   represents the cursor received after querying the noteUri

    this function add the content of each row of the cursor to the
    note list and clear the old list if any and also notifies the
    adapter about the change in data

     */
    private void makeNotesList(Cursor cursor) {
        Single<List<NoteObject>> listSingle = Single.fromCallable(() -> {
            List<NoteObject> tempList = new ArrayList<>();
            NoteObject object = null;
            FileOperation operation = new FileOperation(getActivity());
            while (cursor != null && cursor.moveToNext()) {
                try {
                    object = operation.readFile(cursor.getString(cursor.getColumnIndex(TableNames.table1.mFileName)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (object != null) {
                    tempList.add(object);
                }
            }
            return tempList;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        listSingle.subscribe(new SingleObserver<List<NoteObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<NoteObject> noteObjects) {
                if (noteObjects != null) {
                    mNotesList.clear();
                    mNotesList.addAll(noteObjects);
                    mNotesAdapter.updateNotesList(noteObjects);
                    setEmpty();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.getMessage());
            }
        });
    }

    /*
   @param cursor   represents the cursor received after querying the folderUri

   this function add the content of each row of the cursor to the
   folder list and clear the old list if any and also notifies the
   adapter about the change in data

    */
    private void makeFolderList(Cursor cursor) {
        Single<List<FolderObject>> listSingle = Single.fromCallable(() -> {
            List<FolderObject> tempList = new ArrayList<>();
            while (cursor != null && cursor.moveToNext()) {
                tempList.add(new FolderObject(cursor.getString(cursor.getColumnIndex(TableNames.table2.mFolderName))
                        , cursor.getString(cursor.getColumnIndex(TableNames.table2.mColor))));
            }
            return tempList;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        listSingle.subscribe(new SingleObserver<List<FolderObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<FolderObject> folderNames) {
                if (folderNames != null) {
                    mFolderList.clear();
                    mFolderList.addAll(folderNames);
                    mNotesAdapter.updateFolderList(folderNames);
                    setEmpty();
                }
            }
            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.getMessage());
            }
        });
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

    @Override
    public void onColorSelected(String color) {
        mColor = color;
        if (mColorView != null) {
            mColorView.setBackgroundTintList(stateList(mColor));
        }
    }
}
