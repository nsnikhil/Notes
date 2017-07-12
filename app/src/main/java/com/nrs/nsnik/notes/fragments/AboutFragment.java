package com.nrs.nsnik.notes.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/*
 this fragments shows
 info about the app and all the
 3rd party libraries used to make this app
 and also the license info
 */

public class AboutFragment extends Fragment {

    private final String[] mLibraryNames = {"Butter Knife", "RxAndroid", "RxJava", "Glide", "LeakCanary", "Timber"};
    private final String[] mLibraryLinks = {"https://github.com/JakeWharton/butterknife", "https://github.com/ReactiveX/RxAndroid",
            "https://github.com/ReactiveX/RxJava", "https://github.com/bumptech/glide", "https://github.com/square/leakcanary"
            , "https://github.com/JakeWharton/timber"};

    @BindView(R.id.aboutVersionInfo)
    TextView mVersionNo;
    @BindView(R.id.aboutLibraries)
    Button mLibraries;
    @BindView(R.id.aboutLicense)
    Button mLicense;
    private Unbinder mUnbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        return v;
    }

    private void initialize() {
        mVersionNo.setText(BuildConfig.VERSION_NAME);
    }

    private void listeners() {
        mLibraries.setOnClickListener(view -> showLibrariesList());
        mLicense.setOnClickListener(view -> chromeCustomTab(getActivity().getResources().getString(R.string.aboutLicenseUrl)));
    }

    /*
    @param url  display the url supplied im a chrome custom tab
     */
    private void chromeCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        builder.setSecondaryToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        builder.setExitAnimations(getActivity(), android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
    }

    /*
    displays the list of available libraries in a
    dialog format and open links of each library
    in a chrome custom tab
     */
    private void showLibrariesList() {
        AlertDialog.Builder choosePath = new AlertDialog.Builder(getActivity());
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
