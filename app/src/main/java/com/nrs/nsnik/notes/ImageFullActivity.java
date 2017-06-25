package com.nrs.nsnik.notes;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nrs.nsnik.notes.adapters.ImageAdapter;
import com.nrs.nsnik.notes.interfaces.SendSize;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFullActivity extends AppCompatActivity implements SendSize{

    @BindView(R.id.fullImage)RecyclerView mImageList;
    @BindView(R.id.fullToolBar)Toolbar mImageToolbar;
    private static final String TAG = ImageFullActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full);
        ButterKnife.bind(this);
        initialize();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void initialize(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setSupportActionBar(mImageToolbar);
        mImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        if (getIntent() != null) {
            mImageList.setAdapter(null);
            mImageList.getLayoutManager().scrollToPosition(getIntent().getExtras().getInt(getResources().getString(R.string.intentArrayListPosition)));
            ArrayList<Bitmap> mImages = new ArrayList<>();
            ImageAdapter adapter = new ImageAdapter(this,mImages,this);
            mImageList.setAdapter(adapter);
        }
    }

    @Override
    public void validateSize(int position) {
        Log.d(TAG, String.valueOf(position));
    }
}
