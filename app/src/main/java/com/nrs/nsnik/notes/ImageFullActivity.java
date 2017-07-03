package com.nrs.nsnik.notes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;

import com.nrs.nsnik.notes.adapters.ImageAdapter;
import com.nrs.nsnik.notes.interfaces.SendSize;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
Receives list of file names as intent which are passed to adapter
that get the bitmap/images of that file and displays them in full
screen
 */
public class ImageFullActivity extends AppCompatActivity implements SendSize {

    @BindView(R.id.fullImage)
    RecyclerView mImageList;
    private static final String TAG = ImageFullActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        initialize();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initialize() {
        mImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        if (getIntent() != null) {
            Bundle bundle = getIntent().getBundleExtra(getResources().getString(R.string.bundleIntentImage));
            int currPos = bundle.getInt(getResources().getString(R.string.bundleArrayListPosition));
            ArrayList<String> mImagesLoc = bundle.getStringArrayList(getResources().getString(R.string.bundleStringImageArray));
            ImageAdapter adapter = new ImageAdapter(this, mImagesLoc, this, true);
            mImageList.setAdapter(adapter);
            mImageList.getLayoutManager().scrollToPosition(currPos);
        }
    }

    @Override
    public void validateSize(int position) {
        Log.d(TAG, String.valueOf(position));
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
