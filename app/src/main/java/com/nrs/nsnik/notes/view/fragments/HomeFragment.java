/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.data.FolderDataObserver;
import com.nrs.nsnik.notes.model.data.NoteDataObserver;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.FolderObject;
import com.nrs.nsnik.notes.model.objects.NoteObject;
import com.nrs.nsnik.notes.util.DatabaseOperations;
import com.nrs.nsnik.notes.util.FileOperation;
import com.nrs.nsnik.notes.util.RvItemTouchHelper;
import com.nrs.nsnik.notes.util.events.ColorPickerEvent;
import com.nrs.nsnik.notes.util.interfaces.NoteObserver;
import com.nrs.nsnik.notes.view.Henson;
import com.nrs.nsnik.notes.view.MyApplication;
import com.nrs.nsnik.notes.view.adapters.NotesAdapter;
import com.nrs.nsnik.notes.view.fragments.dialogFragments.ColorPickerDialogFragment;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/*
this fragment passes the uri to the adapter upon
which adapter queries and makes a list to display
 */

public class HomeFragment extends Fragment implements NoteObserver {

    @Nullable
    @BindView(R.id.commonList)
    RecyclerView mList;
    @Nullable
    @BindView(R.id.homeEmptyState)
    TextView mEmpty;
    @Nullable
    @BindView(R.id.fabAdd)
    FloatingActionButton mAddSpinner;
    @Nullable
    @BindView(R.id.fabAddNote)
    FloatingActionButton mAddNote;
    @Nullable
    @BindView(R.id.fabAddFolder)
    FloatingActionButton mAddFolder;
    @Nullable
    @BindView(R.id.fabNoteContainer)
    LinearLayout mNoteContainer;
    @Nullable
    @BindView(R.id.fabFolderContainer)
    LinearLayout mFolderContainer;
    @Nullable
    @BindView(R.id.commonListSwipe)
    SwipeRefreshLayout mSwipeRefresh;
    @Nullable
    @BindView(R.id.homeAdView)
    AdView mAdView;
    @Nullable
    @BindView(R.id.homeContainer)
    CoordinatorLayout mHomeContainer;

    @Nullable
    private String mFolderName = "nofolder";
    @Nullable
    private String mColor;
    private ImageView mColorView;

    private Unbinder mUnbinder;
    private List<NoteObject> mNotesList;
    private List<FolderObject> mFolderList;
    @Nullable
    private NotesAdapter mNotesAdapter;

    private FileOperation mFileOperation;
    private DatabaseOperations mDatabaseOperations;

    private CompositeDisposable mCompositeDisposable;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        MobileAds.initialize(getActivity(), getActivity().getResources().getString(R.string.adAdMobId));
        mUnbinder = ButterKnife.bind(this, v);
        mFileOperation = ((MyApplication) getActivity().getApplicationContext()).getFileOperations();
        mDatabaseOperations = ((MyApplication) getActivity().getApplicationContext()).getDatabaseOperations();
        initialize();
        listeners();
        return v;
    }

    /**
     * if the fragment if attached to activity which was
     * launch via intent then get the folder name
     * and append to base content uri
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
        if (mList != null) {
            mList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
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

        mCompositeDisposable = new CompositeDisposable();

        /*
        if the build is not debug
        enable ads
         */
        if (!BuildConfig.DEBUG) {
            AdRequest adRequest = new AdRequest.Builder().build();
            if (mAdView != null) {
                mAdView.loadAd(adRequest);
            }
        }
    }

    private void listeners() {
        if (mAddSpinner != null) {
            mCompositeDisposable.add(RxView.clicks(mAddSpinner).subscribe(v -> {
                if (mNoteContainer != null && mNoteContainer.getVisibility() == View.INVISIBLE || mFolderContainer != null && mFolderContainer.getVisibility() == View.INVISIBLE) {
                    reveal();
                } else {
                    disappear();
                }
            }));
        }
        if (mAddNote != null) {
            mCompositeDisposable.add(RxView.clicks(mAddNote).subscribe(v -> {
                disappear();

                Intent intent = Henson.with(getActivity())
                        .gotoNewNoteActivity()
                        .mNoteId(0)
                        .mFolderNameBundle(mFolderName)
                        .build();

                startActivity(intent);
            }));
        }
        if (mAddFolder != null) {
            mCompositeDisposable.add(RxView.clicks(mAddFolder).subscribe(v -> {
                disappear();
                createFolderDialog();
            }));
        }
        if (mSwipeRefresh != null) {
            mCompositeDisposable.add(RxSwipeRefreshLayout.refreshes(mSwipeRefresh).subscribe(v -> new Handler().postDelayed(() -> mSwipeRefresh.setRefreshing(false), 1000)));
        }
    }

    /**
     * if the add notes and folder fab is not visible
     * the perform animation and then make them visible
     */
    private void reveal() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 135, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(50);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            final Animation scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.jump_from_down);

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
                        if (mFolderContainer != null && mNoteContainer != null) {
                            mFolderContainer.setVisibility(View.VISIBLE);
                            mNoteContainer.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                if (mFolderContainer != null && mNoteContainer != null) {
                    mFolderContainer.startAnimation(scaleUp);
                    mNoteContainer.startAnimation(scaleUp);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (mAddSpinner != null) {
            mAddSpinner.startAnimation(rotateAnimation);
        }
    }

    /**
     * if the add notes and folder fab is visible
     * the perform animation and then make them in-visible
     */
    private void disappear() {
        RotateAnimation rotateAnimation = new RotateAnimation(135, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(50);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            final Animation scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.jump_to_down);

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
                        if (mFolderContainer != null && mNoteContainer != null) {
                            mFolderContainer.setVisibility(View.INVISIBLE);
                            mNoteContainer.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                if (mFolderContainer != null && mNoteContainer != null) {
                    mFolderContainer.startAnimation(scaleDown);
                    mNoteContainer.startAnimation(scaleDown);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (mAddSpinner != null) {
            mAddSpinner.startAnimation(rotateAnimation);
        }
    }

    /**
     * create a alert dialog with custom view
     * to take a name from field
     */
    private void createFolderDialog() {
        AlertDialog.Builder newFolder = new AlertDialog.Builder(getActivity());
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_folder_dialog, null);
        newFolder.setView(v);
        final EditText editText = v.findViewById(R.id.dialogFolderName);
        mColorView = v.findViewById(R.id.dialogFolderColor);
        editText.requestFocus();
        mColor = null;
        mCompositeDisposable.add(RxView.clicks(mColorView).subscribe(o -> {
            ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
            // pickerDialogFragment.setTargetFragment(this, 129);
            pickerDialogFragment.show(getFragmentManager(), "color");
        }));
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            newFolder.setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0));
            newFolder.setPositiveButton(getResources().getString(R.string.create), (dialogInterface, i) -> {
                if (!editText.getText().toString().isEmpty()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    if (mColor == null) {
                        mColor = "#333333";
                    }
                    mDatabaseOperations.insertFolder(TableNames.mFolderContentUri, editText.getText().toString(), mFolderName, mColor);
                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.errorNoFolderName), Toast.LENGTH_SHORT).show();
                }
            });
        }
        newFolder.create().show();
    }

    private void cleanUp() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
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
    public void updateItems(@NonNull Cursor cursor) {
        if (cursor.getColumnIndex(TableNames.table1.mTitle) != -1) {
            makeNotesList(cursor);
        } else {
            makeFolderList(cursor);
        }
    }

    private void setEmpty() {
        if (mEmpty != null) {
            if (mFolderList.size() <= 0 && mNotesList.size() <= 0) {
                mEmpty.setVisibility(View.VISIBLE);
            } else {
                mEmpty.setVisibility(View.GONE);
            }
        }
    }

    /**
     * @param cursor represents the cursor received after querying the noteUri
     *               <p>
     *               this function add the content of each row of the cursor to the
     *               note list and clear the old list if any and also notifies the
     *               adapter about the change in data
     */
    private void makeNotesList(@Nullable Cursor cursor) {
        Single<List<NoteObject>> listSingle = Single.fromCallable(() -> {
            List<NoteObject> tempList = new ArrayList<>();
            NoteObject object = null;
            while (cursor != null && cursor.moveToNext()) {
                try {
                    object = mFileOperation.readFile(cursor.getString(cursor.getColumnIndex(TableNames.table1.mFileName)));
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
            public void onSuccess(@Nullable List<NoteObject> noteObjects) {
                if (noteObjects != null && mNotesAdapter != null) {
                    mNotesList.clear();
                    mNotesList.addAll(noteObjects);
                    mNotesAdapter.updateNotesList(noteObjects);
                    setEmpty();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
    }

    /**
     * @param cursor represents the cursor received after querying the folderUri
     *               <p>
     *               this function add the content of each row of the cursor to the
     *               folder list and clear the old list if any and also notifies the
     *               adapter about the change in data
     */
    private void makeFolderList(@Nullable Cursor cursor) {
        Single<List<FolderObject>> listSingle = Single.fromCallable(() -> {
            List<FolderObject> tempList = new ArrayList<>();
            while (cursor != null && cursor.moveToNext()) {
                tempList.add(FolderObject.builder()
                        .folderName(cursor.getString(cursor.getColumnIndex(TableNames.table2.mFolderName)))
                        .folderColor(cursor.getString(cursor.getColumnIndex(TableNames.table2.mColor)))
                        .build());
            }
            return tempList;
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        listSingle.subscribe(new SingleObserver<List<FolderObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(@Nullable List<FolderObject> folderNames) {
                if (folderNames != null && mNotesAdapter != null) {
                    mFolderList.clear();
                    mFolderList.addAll(folderNames);
                    mNotesAdapter.updateFolderList(folderNames);
                    setEmpty();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.d(e.getMessage());
            }
        });
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onColorPickerEvent(ColorPickerEvent colorPickerEvent) {
        mColor = colorPickerEvent.getColor();
        if (mColorView != null) {
            mColorView.setBackgroundTintList(stateList(mColor));
        }
    }
}