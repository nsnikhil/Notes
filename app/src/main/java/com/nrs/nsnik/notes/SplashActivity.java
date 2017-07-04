package com.nrs.nsnik.notes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.squareup.leakcanary.RefWatcher;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            Intent startIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(startIntent);
            finish();
        }, 1000);
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
