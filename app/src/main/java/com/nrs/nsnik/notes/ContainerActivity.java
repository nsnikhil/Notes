package com.nrs.nsnik.notes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.nrs.nsnik.notes.fragments.HomeFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;

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
