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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.nrs.nsnik.notes.BuildConfig;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.data.TableNames;
import com.nrs.nsnik.notes.model.objects.SearchObject;
import com.nrs.nsnik.notes.view.adapters.SearchAdapter;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class SearchActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.searchToolBar)
    Toolbar mSearchToolbar;
    @Nullable
    @BindView(R.id.searchList)
    RecyclerView mSearchList;
    @Nullable
    @BindView(R.id.searchText)
    EditText mSearchText;
    @Nullable
    @BindView(R.id.searchEmptyState)
    TextView mEmptyState;
    private PublishSubject<String> mSubject;
    private String mCurrentSearch;
    private SearchAdapter mSearchAdapter;
    private List<SearchObject> mQueryList;

    private CompositeDisposable mCompositeDisposable;

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
        if (mSearchList != null) {
            mSearchList.setLayoutManager(new LinearLayoutManager(this));
        }
        mSearchAdapter = new SearchAdapter(this, mQueryList);
        mSearchList.setAdapter(mSearchAdapter);
        if (mEmptyState != null) {
            mEmptyState.setText(getResources().getString(R.string.emptyStateSearch));
        }
        /*
        Subject is special in a sense that its is both the
        observer and observable i.e. you can pass data using
        subject and get data using subject
         */
        mSubject = PublishSubject.create();

        mCompositeDisposable = new CompositeDisposable();
    }

    private void listeners() {
        if (mSearchText != null && mEmptyState != null) {
            mCompositeDisposable.add(RxTextView.textChanges(mSearchText).debounce(200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(charSequence -> {
                if (!charSequence.toString().isEmpty() && charSequence.toString().length() > 0) {
                    mCurrentSearch = charSequence.toString();
                    performSearch(mCurrentSearch, false);
                } else {
                    mEmptyState.setText(getResources().getString(R.string.emptyStateSearchNoString));
                }
            }));
            mCompositeDisposable.add(RxTextView.editorActionEvents(mSearchText).observeOn(AndroidSchedulers.mainThread()).subscribe(textViewEditorActionEvent -> {
                if (textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_SEARCH) {
                    if (!textViewEditorActionEvent.view().getText().toString().isEmpty() && textViewEditorActionEvent.view().getText().toString().length() > 0) {
                        mCurrentSearch = textViewEditorActionEvent.view().getText().toString();
                        performSearch(mCurrentSearch, true);
                    } else {
                        mEmptyState.setText(getResources().getString(R.string.emptyStateSearchNoString));
                    }
                }
            }));
        }
    }

    /**
     * @param text             the search query text
     * @param fromKeyPress     flag to check if search button on keyboard was clicked or not
     */
    private void performSearch(String text, boolean fromKeyPress) {
        mQueryList.clear();
        mSearchAdapter.modifyList(mQueryList);
        if (mSearchText != null) {
            mSearchText.clearFocus();
        }
        if (fromKeyPress) {
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (in != null) {
                in.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
            }
        }
        //Call the next method on subject
        mSubject.onNext(text);
    }

    private void initializeSubject() {
        mSubject.subscribeOn(Schedulers.io())
                .map(this::getSearchList).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.Observer<List<SearchObject>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@android.support.annotation.NonNull @NonNull List<SearchObject> searchObjects) {
                        if (mEmptyState != null) {
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
                    }

                    @Override
                    public void onError(@android.support.annotation.NonNull @NonNull Throwable e) {
                        Timber.d(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("search complete");
                    }
                });

    }

    /**
     * @param s the folder name or note tile that will be searched
     */
    @android.support.annotation.NonNull
    private List<SearchObject> getSearchList(String s) {
        List<SearchObject> mList = new ArrayList<>();
        String query = "search/" + s;
        Cursor folderCursor = getContentResolver().query(Uri.withAppendedPath(TableNames.mFolderContentUri, query), null, null, null, null);
        Cursor noteCursor = getContentResolver().query(Uri.withAppendedPath(TableNames.mContentUri, query), null, null, null, null);
        try {
            while (folderCursor != null && folderCursor.moveToNext()) {
                String folderName = folderCursor.getString(folderCursor.getColumnIndex(TableNames.table2.mFolderName));
                mList.add(SearchObject.builder()
                        .setName(folderName)
                        .setIsFolder(true)
                        .build());
            }
            while ((noteCursor != null && noteCursor.moveToNext())) {
                String noteTitle = noteCursor.getString(noteCursor.getColumnIndex(TableNames.table1.mTitle));
                mList.add(SearchObject.builder()
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
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
            mCompositeDisposable.dispose();
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
