/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nrs.nsnik.notes.fragments.HomeFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
  this activity is usually called by folder click and
  the name of the folder is passed as extras
  which is appended to a uri upon which query is performed to
  get data list
 */

public class ContainerActivity extends AppCompatActivity {

    @BindView(R.id.containerToolbar)
    Toolbar mContainerToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        ButterKnife.bind(this);
        initialize();
        listeners();
        setFolderValues();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initialize() {
        setSupportActionBar(mContainerToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /*
    this function get the folder name from intent
    passes it to fragment which displays the notes list
    in that folder
     */
    private void setFolderValues() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            String folderName = getIntent().getExtras().getString(getResources().getString(R.string.intentFolderName));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(folderName);
            }
            HomeFragment homeFragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putString(getResources().getString(R.string.homefldnm), folderName);
            homeFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.containerSpace, homeFragment).commit();
        }
    }

    private void listeners() {
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
