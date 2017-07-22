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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.SearchObject;
import com.nrs.nsnik.notes.view.adapters.SearchAdapter;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.searchToolBar)
    Toolbar mSearchToolbar;
    @BindView(R.id.searchList)
    RecyclerView mSearchList;
    @BindView(R.id.searchText)
    EditText mSearchText;
    @BindView(R.id.searchEmptyState)
    TextView mEmptyState;
    private PublishSubject<String> mSubject;
    private String mCurrentSearch;
    private SearchAdapter mSearchAdapter;
    private List<SearchObject> mQueryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        initialize();
        listeners();
        initializeSubject();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initialize() {
        setSupportActionBar(mSearchToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mQueryList = new ArrayList<>();
        mSearchList.setLayoutManager(new LinearLayoutManager(this));
        mSearchAdapter = new SearchAdapter(this, mQueryList);
        mSearchList.setAdapter(mSearchAdapter);
        mEmptyState.setText(getResources().getString(R.string.emptyStateSearch));
        /*
        Subject is special in a sense that its is both the
        observer and observable i.e. you can pass data using
        subject and get data using subject
         */
        mSubject = PublishSubject.create();
    }

    private void listeners() {
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty() && editable.toString().length() > 0) {
                    mCurrentSearch = editable.toString();
                    performSearch(mCurrentSearch, false);
                } else {
                    mEmptyState.setText(getResources().getString(R.string.emptyStateSearchNoString));
                }
            }
        });
        mSearchText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!textView.getText().toString().isEmpty() && textView.getText().toString().length() > 0) {
                    mCurrentSearch = textView.getText().toString();
                    performSearch(mCurrentSearch, true);
                } else {
                    mEmptyState.setText(getResources().getString(R.string.emptyStateSearchNoString));
                }
                return true;
            }
            return false;
        });
    }

    /*
    @param text             the search query text
    @param fromKeyPress     flag to check if search button on keyboard was clicked or not
     */
    private void performSearch(String text, boolean fromKeyPress) {
        mQueryList.clear();
        mSearchAdapter.modifyList(mQueryList);
        mSearchText.clearFocus();
        if (fromKeyPress) {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
        }
        //Class the next method on subject
        mSubject.onNext(text);
    }

    /*

     */
    private void initializeSubject() {
        mSubject.subscribeOn(Schedulers.io())
                .map(this::getSearchList).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<List<SearchObject>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<SearchObject> searchObjects) {
                        if (searchObjects.size() > 0) {
                            mEmptyState.setVisibility(View.GONE);
                            mQueryList = searchObjects;
                            mSearchAdapter.modifyList(mQueryList);
                        } else {
                            String noResults = getResources().getString(R.string.emptyStateSearch, mCurrentSearch);
                            mEmptyState.setText(noResults);
                            mEmptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Timber.d(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("search complete");
                    }
                });

    }

    /*
    @param s        the folder name or note tile that will be searched
     */
    private List<SearchObject> getSearchList(String s) {
        List<SearchObject> mList = new ArrayList<>();
        String query = "search/" + s;
        Cursor folderCursor = getContentResolver().query(Uri.withAppendedPath(TableNames.mFolderContentUri, query), null, null, null, null);
        Cursor noteCursor = getContentResolver().query(Uri.withAppendedPath(TableNames.mContentUri, query), null, null, null, null);
        try {
            while (folderCursor != null && folderCursor.moveToNext()) {
                String folderName = folderCursor.getString(folderCursor.getColumnIndex(TableNames.table2.mFolderName));
                mList.add(new SearchObject.SearchObjectBuilder()
                        .setName(folderName)
                        .setIsFolder(true)
                        .build());
            }
            while ((noteCursor != null && noteCursor.moveToNext())) {
                String noteTitle = noteCursor.getString(noteCursor.getColumnIndex(TableNames.table1.mTitle));
                mList.add(new SearchObject.SearchObjectBuilder()
                        .setName(noteTitle)
                        .setIsFolder(false)
                        .build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (folderCursor != null) {
                folderCursor.close();
            }
            if (noteCursor != null) {
                noteCursor.close();
            }
        }
        return mList;
    }


    private void cleanUp() {
        if (mSubject != null) {
            mSubject.unsubscribeOn(Schedulers.io());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUp();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }

}
