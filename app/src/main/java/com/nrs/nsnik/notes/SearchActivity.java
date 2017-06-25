package com.nrs.nsnik.notes;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.searchToolBar)Toolbar mSearchToolbar;
    @BindView(R.id.searchList)RecyclerView mSearchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        initialize();
        listeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initialize(){
        setSupportActionBar(mSearchToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void listeners(){
    }
}
