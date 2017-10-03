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
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.view.MyApplication;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

/*
 this fragments shows
 info about the app and all the
 3rd party libraries used to make this app
 and also the license info
 */

public class AboutFragment extends Fragment {

    private final String[] mLibraryNames = {
            "Android Support Library",
            "FindBug JSR305",
            "FireBase",
            "Google Play Services",
            "RxAndroid",
            "RxJava",
            "RxBinding",
            "Chrome custom tabs",
            "Timber",
            "Guava",
            "Dagger 2",
            "Butter Knife",
            "Glide",
            "RollBar",
            "EventBus",
            "AutoValue",
            "Auto Parcel",
            "Leak Canary",
            "Block Canary"
    };


    private final String[] mLibraryLinks = {
            "https://github.com/android/platform_frameworks_support",
            "https://github.com/spotbugs/spotbugs",
            "https://firebase.google.com/terms/",
            "https://developers.google.com/terms/",
            "https://github.com/ReactiveX/RxAndroid",
            "https://github.com/ReactiveX/RxJava",
            "https://github.com/JakeWharton/RxBinding",
            "https://github.com/GoogleChrome/custom-tabs-client",
            "https://github.com/JakeWharton/timber",
            "https://github.com/google/guava",
            "https://google.github.io/dagger/",
            "https://github.com/JakeWharton/butterknife",
            "https://github.com/bumptech/glide",
            "https://github.com/rollbar/rollbar-android",
            "https://github.com/greenrobot/EventBus",
            "https://github.com/google/auto/tree/master/value",
            "https://github.com/rharter/auto-value-parcel",
            "https://github.com/square/leakcanary",
            "https://github.com/markzhai/AndroidPerformanceMonitor"
    };

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
        if (mLicense != null) {
            mCompositeDisposable.add(RxView.clicks(mLicense).subscribe(v -> chromeCustomTab(getActivity().getResources().getString(R.string.aboutLicenseUrl))));
        }
    }

    /**
     * @param url  display the url supplied im a chrome custom tab
     */
    private void chromeCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        builder.setSecondaryToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        builder.setExitAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
    }

    /**
     * displays the list of available libraries in a
     * dialog format and open links of each library
     * in a chrome custom tab
     */
    private void showLibrariesList() {
        AlertDialog.Builder choosePath = new AlertDialog.Builder(getActivity());
        choosePath.setTitle(getActivity().getResources().getString(R.string.aboutLibrariesHead));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        for (String aLibraryName : mLibraryNames) {
            arrayAdapter.add(aLibraryName);
        }
        choosePath.setAdapter(arrayAdapter, (dialog, position) -> chromeCustomTab(mLibraryLinks[position]));
        choosePath.create().show();
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
}
