package com.nrs.nsnik.notes;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.nrs.nsnik.notes.adapters.ObserverAdapter;
import com.nrs.nsnik.notes.adapters.SearchAdapter;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.FolderCount;
import com.nrs.nsnik.notes.interfaces.NotesCount;
import com.nrs.nsnik.notes.interfaces.Observer;
import com.nrs.nsnik.notes.objects.NoteObject;
import com.nrs.nsnik.notes.objects.SearchObject;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SearchActivity extends AppCompatActivity implements NotesCount,FolderCount{

    @BindView(R.id.searchToolBar)Toolbar mSearchToolbar;
    @BindView(R.id.searchList)RecyclerView mSearchList;
    @BindView(R.id.searchText)EditText mSearchText;
    @BindView(R.id.searchEmptyState)TextView mEmptyState;
    private PublishSubject<String> mSubject;
    private SearchAdapter mSearchAdapter;
    private List<SearchObject> mQueryList;
    private static final String TAG = SearchActivity.class.getSimpleName();

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

    private void initialize(){
        setSupportActionBar(mSearchToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mQueryList = new ArrayList<>();
        mSearchList.setLayoutManager(new LinearLayoutManager(this));
        mSearchAdapter = new SearchAdapter(this,mQueryList);
        mSearchList.setAdapter(mSearchAdapter);
    }

    private void listeners(){
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH){
                    performSearch(textView.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch(String text){
        mQueryList.clear();
        mSearchText.clearFocus();
        InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
        mSubject.onNext(text);
    }

    private void initializeSubject(){
        mSubject = PublishSubject.create();
        mSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(new Function<String, List<SearchObject>>() {
                    @Override
                    public List<SearchObject> apply(@NonNull String s) throws Exception {
                        return getSearchList(s);
                    }
                }).subscribe(new io.reactivex.Observer<List<SearchObject>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<SearchObject> searchObjects) {
                        if(searchObjects.size()>0) {
                            mEmptyState.setVisibility(View.GONE);
                            mQueryList = searchObjects;
                            mSearchAdapter.modifyList(mQueryList);
                        }else {
                            mEmptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private List<SearchObject> getSearchList(String s){
        List<SearchObject> mList = new ArrayList<>();
        String query = "search/"+s;
        Cursor folderCursor = getContentResolver().query(Uri.withAppendedPath(TableNames.mFolderContentUri,query),null,null,null,null);
        Cursor noteCursor = getContentResolver().query(Uri.withAppendedPath(TableNames.mContentUri,query),null,null,null,null);
        try {
            while (folderCursor!=null&&folderCursor.moveToNext()){
                String folderName = folderCursor.getString(folderCursor.getColumnIndex(TableNames.table2.mFolderName));
                mList.add(new SearchObject(folderName,false));
            }while ((noteCursor!=null&&noteCursor.moveToNext())){
                String noteTitle = noteCursor.getString(noteCursor.getColumnIndex(TableNames.table1.mTitle));
                mList.add(new SearchObject(noteTitle,true));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(folderCursor!=null){
                folderCursor.close();
            }if(noteCursor!=null){
                noteCursor.close();
            }
        }
        return mList;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BuildConfig.DEBUG) {
            RefWatcher refWatcher = MyApplication.getRefWatcher(this);
            refWatcher.watch(this);
        }
    }

    @Override
    public void getNotesCount(int count) {

    }

    @Override
    public void getFolderCount(int count) {

    }

}
