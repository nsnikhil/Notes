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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.util.events.FolderClickEvent;
import com.nrs.nsnik.notes.view.Henson;
import com.nrs.nsnik.notes.view.fragments.dialogFragments.CreateFolderDialog;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class HomeFragment extends Fragment {

    @Nullable
    @BindView(R.id.fabAdd)
    FloatingActionButton mAddSpinner;

    @Nullable
    @BindView(R.id.fabAddNote)
    FloatingActionButton mAddNote;

    @Nullable
    @BindView(R.id.fabAddFolder)
    FloatingActionButton mAddFolder;

    @BindView(R.id.fabAddNoteContainer)
    LinearLayout mAddNoteFabContainer;

    @BindView(R.id.fabAddFolderContainer)
    LinearLayout mAddFolderFabContainer;

    private boolean mIsVisible;

    @Nullable
    private String mFolderName = "nofolder";
    private Unbinder mUnbinder;
    private CompositeDisposable mCompositeDisposable;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        setHasOptionsMenu(true);
        return v;
    }

    private void getArgs() {
        if (getActivity() != null && getArguments() != null) {
            mFolderName = getArguments().getString(getActivity().getResources().getString(R.string.homefldnm));
        }
    }

    private void initialize() {
        getArgs();
        mCompositeDisposable = new CompositeDisposable();
        attachFragment(mFolderName);
    }

    public void attachFragment(String folderName) {

        if (getFragmentManager() != null && getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(getActivity().getResources().getString(R.string.bundleListFragmentFolderName), folderName);
            ListFragment listFragment = new ListFragment();
            listFragment.setArguments(bundle);

            getFragmentManager().beginTransaction().addToBackStack("backStack").replace(R.id.homeContainer, listFragment)
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .commit();
        }
    }

    private void listeners() {
        if (mAddSpinner != null) {
            mCompositeDisposable.add(RxView.clicks(mAddSpinner).subscribe(o -> {
                if (mIsVisible) {
                    disappear();
                } else {
                    reveal();
                }
            }, throwable -> Timber.d(throwable.getMessage())));
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
                Bundle bundle = new Bundle();
                if (getActivity() != null && getFragmentManager() != null) {
                    bundle.putString(getActivity().getResources().getString(R.string.bundleCreateFolderParentFolder), mFolderName);
                    CreateFolderDialog createFolderDialog = new CreateFolderDialog();
                    createFolderDialog.setArguments(bundle);
                    createFolderDialog.show(getFragmentManager(), "createFolder");
                }
            }));
        }
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
    public void onFolderClickEvent(FolderClickEvent folderClickEvent) {
        mFolderName = folderClickEvent.getFolderName();
        attachFragment(mFolderName);
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
                        mIsVisible = true;
                        mAddNoteFabContainer.setVisibility(View.VISIBLE);
                        mAddFolderFabContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mAddFolderFabContainer.startAnimation(scaleUp);
                mAddNoteFabContainer.startAnimation(scaleUp);
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
                        mIsVisible = false;
                        mAddNoteFabContainer.setVisibility(View.INVISIBLE);
                        mAddFolderFabContainer.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mAddFolderFabContainer.startAnimation(scaleDown);
                mAddNoteFabContainer.startAnimation(scaleDown);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (mAddSpinner != null) {
            mAddSpinner.startAnimation(rotateAnimation);
        }
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
        if (BuildConfig.DEBUG && getActivity() != null) {
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
}