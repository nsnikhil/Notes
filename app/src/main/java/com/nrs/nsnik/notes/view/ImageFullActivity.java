/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.util.FileOperation;
import com.nrs.nsnik.notes.util.interfaces.OnItemRemoveListener;
import com.nrs.nsnik.notes.view.adapters.ImageAdapter;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/*
Receives list of file names as intent which are passed to adapter
that get the bitmap/images of that file and displays them in full
screen
 */
public class ImageFullActivity extends AppCompatActivity implements OnItemRemoveListener {

    @Nullable
    @BindView(R.id.fullImage)
    RecyclerView mImageList;

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
        if (mImageList != null) {
            mImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
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
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }

    @Override
    public void onItemRemoved(int position, FileOperation.FILE_TYPES types, String fileName) {
        Timber.d(String.valueOf(position));
    }
}
