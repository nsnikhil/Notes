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

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.LibraryObject;
import com.squareup.leakcanary.RefWatcher;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

/**
 * this fragments shows
 * info about the app and all the
 * 3rd party libraries used to make this app
 * and also the license info
 */

public class AboutFragment extends Fragment {

    private final List<LibraryObject> mLibraryList = Arrays.asList(

            LibraryObject
                    .builder()
                    .libraryName("Android Support Library")
                    .libraryLink("https://github.com/android/platform_frameworks_support")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Android Testing Support Library")
                    .libraryLink("https://google.github.io/android-testing-support-library/")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Android Lifecycle Components")
                    .libraryLink("https://developer.android.com/topic/libraries/architecture/lifecycle.html")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("AutoValue")
                    .libraryLink("https://github.com/google/auto/tree/master/value")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Auto Parcel")
                    .libraryLink("https://github.com/rharter/auto-value-parcel")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Block Canary")
                    .libraryLink("https://github.com/markzhai/AndroidPerformanceMonitor")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Butter Knife")
                    .libraryLink("https://github.com/JakeWharton/butterknife")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Chrome custom tabs")
                    .libraryLink("https://github.com/GoogleChrome/custom-tabs-client")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Dagger 2")
                    .libraryLink("https://google.github.io/dagger/")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Dart & Henson")
                    .libraryLink("https://github.com/f2prateek/dart")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("EventBus")
                    .libraryLink("https://github.com/greenrobot/EventBus")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("FindBug JSR305")
                    .libraryLink("https://github.com/spotbugs/spotbugs")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("FireBase")
                    .libraryLink("https://firebase.google.com/terms/")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Glide")
                    .libraryLink("https://github.com/bumptech/glide")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Google Play Services")
                    .libraryLink("https://developers.google.com/terms/")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Jetbrains Annotation")
                    .libraryLink("https://mvnrepository.com/artifact/org.jetbrains/annotations/13.0")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("jUnit4")
                    .libraryLink("https://github.com/junit-team/junit4")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Leak Canary")
                    .libraryLink("https://github.com/square/leakcanary")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Mockito")
                    .libraryLink("https://github.com/mockito/mockito")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("OkIO")
                    .libraryLink("https://github.com/square/okio")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Paging")
                    .libraryLink("https://developer.android.com/topic/libraries/architecture/paging.html")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Robelectric")
                    .libraryLink("https://github.com/robolectric/robolectric")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("RollBar")
                    .libraryLink("https://github.com/rollbar/rollbar-android")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Room")
                    .libraryLink("https://developer.android.com/topic/libraries/architecture/room.html")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("RxAndroid")
                    .libraryLink("https://github.com/ReactiveX/RxAndroid")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("RxBinding")
                    .libraryLink("https://github.com/JakeWharton/RxBinding")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("RxJava")
                    .libraryLink("https://github.com/ReactiveX/RxJava")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Serial")
                    .libraryLink("https://github.com/twitter/Serial")
                    .build(),

            LibraryObject
                    .builder()
                    .libraryName("Timber")
                    .libraryLink("https://github.com/JakeWharton/timber")
                    .build()
    );

    @Nullable
    @BindView(R.id.aboutLibraries)
    Button mLibraries;
    @Nullable
    @BindView(R.id.aboutLicense)
    Button mLicense;
    @Nullable
    @BindView(R.id.aboutSourceCode)
    TextView mSourceCode;
    @Nullable
    @BindView(R.id.aboutNikhil)
    TextView mNikhilLinks;
    private Unbinder mUnbinder;

    private CompositeDisposable mCompositeDisposable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        return v;
    }

    private void initialize() {
        if (mSourceCode != null) {
            mSourceCode.setMovementMethod(LinkMovementMethod.getInstance());
        }
        if (mNikhilLinks != null) {
            mNikhilLinks.setMovementMethod(LinkMovementMethod.getInstance());
        }
        mCompositeDisposable = new CompositeDisposable();
    }

    private void listeners() {
        if (mLibraries != null) {
            mCompositeDisposable.add(RxView.clicks(mLibraries).subscribe(v -> showLibrariesList()));
        }
        if (mLicense != null && getActivity() != null) {
            mCompositeDisposable.add(RxView.clicks(mLicense).subscribe(v -> chromeCustomTab(getActivity().getResources().getString(R.string.aboutLicenseUrl))));
        }
    }

    /**
     * @param url display the url supplied im a chrome custom tab
     */
    private void chromeCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (getActivity() != null) {
            builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            builder.setSecondaryToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            builder.setExitAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
        }
    }

    /**
     * displays the list of available libraries in a
     * dialog format and open links of each library
     * in a chrome custom tab
     */
    private void showLibrariesList() {
        if (getActivity() != null) {
            AlertDialog.Builder choosePath = new AlertDialog.Builder(getActivity());
            choosePath.setTitle(getActivity().getResources().getString(R.string.aboutLibrariesHead));
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            for (LibraryObject libraryObject : mLibraryList) {
                arrayAdapter.add(libraryObject.libraryName());
            }
            choosePath.setAdapter(arrayAdapter, (dialog, position) -> chromeCustomTab(mLibraryList.get(position).libraryLink()));
            choosePath.create().show();
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
        if (getActivity() != null && BuildConfig.DEBUG) {
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
