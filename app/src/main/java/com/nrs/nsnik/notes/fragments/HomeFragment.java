package com.nrs.nsnik.notes.fragments;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.nrs.nsnik.notes.adapters.ObserverAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.helpers.RvItemTouchHelper;
import com.nrs.nsnik.notes.interfaces.FolderCount;
import com.nrs.nsnik.notes.interfaces.NotesCount;
import com.squareup.leakcanary.RefWatcher;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeFragment extends Fragment implements NotesCount, FolderCount {

    private static final String TAG = HomeFragment.class.getSimpleName();
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
    private Unbinder mUnbinder;
    private int mNotesCount, mFolderCount;

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

    private void getArgs() {
        if (getArguments() != null) {
            mFolderName = getArguments().getString(getActivity().getResources().getString(R.string.homefldnm));
        }
    }

    private void initialize() {
        getArgs();
        mList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ObserverAdapter adapter = new ObserverAdapter(getActivity(), TableNames.mContentUri, TableNames.mFolderContentUri
                , this, this, getLoaderManager(), mFolderName);
        mList.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new RvItemTouchHelper(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mList);
        if (!BuildConfig.DEBUG) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }


    private void listeners() {
        mAddSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNoteContainer.getVisibility() == View.INVISIBLE || mFolderContainer.getVisibility() == View.INVISIBLE) {
                    reveal();
                } else {
                    disappear();
                }
            }
        });
        mAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disappear();
                Intent newNote = new Intent(getActivity(), NewNoteActivity.class);
                newNote.putExtra(getActivity().getResources().getString(R.string.newnotefolderbundle), mFolderName);
                startActivity(newNote);
            }
        });
        mAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disappear();
                createFolderDialog();
            }
        });
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefresh.setRefreshing(false);
                    }
                }, 1000);

            }
        });
    }


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

    private void createFolderDialog() {
        AlertDialog.Builder newFolder = new AlertDialog.Builder(getActivity());
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.new_folder_dialog, null);
        newFolder.setView(v);
        final EditText editText = v.findViewById(R.id.dialogFolderName);
        editText.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        newFolder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
        newFolder.setPositiveButton(getResources().getString(R.string.create), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                createFolder(editText.getText().toString());
            }
        });
        newFolder.create().show();
    }

    private void createFolder(String name) {
        ContentValues cv = new ContentValues();
        cv.put(TableNames.table2.mFolderName, name);
        Calendar c = Calendar.getInstance();
        cv.put(TableNames.table2.mFolderId, c.getTimeInMillis() + name);
        cv.put(TableNames.table2.mParentFolderName, mFolderName);
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

    private void setEmpty() {
        if (mNotesCount == 0 && mFolderCount == 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void getNotesCount(int count) {
        mNotesCount = count;
        setEmpty();
    }

    @Override
    public void getFolderCount(int count) {
        mFolderCount = count;
        setEmpty();
    }
}
