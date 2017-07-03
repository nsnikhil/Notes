package com.nrs.nsnik.notes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.squareup.leakcanary.RefWatcher;

public class IntroActivity extends AppCompatActivity {

    /*
    TODO INTRO ACTIVITY
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }
}
