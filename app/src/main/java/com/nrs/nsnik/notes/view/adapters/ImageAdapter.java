/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.view.Henson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

/**
 * IMAGE ADAPTER TAKES IN LIST OF STRING WHERE EACH
 * STRING REPRESENTS A FILE NAME, IT USES THIS FILENAME TO
 * CREATE A NEW BITMAP FILE AND DISPLAY EACH IMAGE IN ITS
 * RECYCLER VIEW, IT ALSO TAKES A onItemRemoveListener INTERFACE WHICH
 * IS RESPONSIBLE FOR LETTING THE RECYCLER VIEWS ACTIVITY/FRAGMENT
 * KNOW ABOUT THE CHANGE IN SIZE OF THE ADAPTER
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private final RequestManager mGlideRequestManager;

    private final Context mContext;
    private final boolean mFullScreen;
    private final List<String> mImageLoc;
    @NonNull
    private final CompositeDisposable mCompositeDisposable;

    /**
     * @param c              the context object
     * @param imageLocations the location of images
     * @param forFullScreen  is full screen
     */
    public ImageAdapter(Context c, List<String> imageLocations, boolean forFullScreen) {
        mContext = c;
        mImageLoc = imageLocations;
        mFullScreen = forFullScreen;
        mGlideRequestManager = ((MyApplication) mContext.getApplicationContext()).getRequestManager();
        mCompositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_image_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageAdapter.MyViewHolder holder, int position) {
        if (holder.image != null) {
            mGlideRequestManager
                    .load(new File(((MyApplication) mContext.getApplicationContext()).getRootFolder(), mImageLoc.get(position)).toString())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.image.setVisibility(View.VISIBLE);
                            if (holder.mProgress != null) {
                                holder.mProgress.setVisibility(View.GONE);
                            }
                            return false;
                        }
                    })
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        if (mImageLoc != null) {
            return mImageLoc.size();
        }
        return 0;
    }

    private void cleanUp() {
        mCompositeDisposable.clear();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        cleanUp();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.singleImage)
        ImageView image;
        @Nullable
        @BindView(R.id.singleImageCancel)
        ImageView remove;
        @Nullable
        @BindView(R.id.singleImageProgress)
        ProgressBar mProgress;

        MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            /*
            if the adapter is set for a full screen
            activity/fragment then remove the option
            remove the image or click on image
             */
            if (remove != null && image != null) {
                if (mFullScreen) {
                    remove.setVisibility(View.GONE);
                    image.setScaleType(ImageView.ScaleType.CENTER);
                } else {
                    mCompositeDisposable.add(RxView.clicks(remove).subscribe(v -> {

                    }));
                    mCompositeDisposable.add(RxView.clicks(image).subscribe(v -> {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {

                            Intent intent = Henson.with(mContext)
                                    .gotoImageFullActivity()
                                    .mPosition(getAdapterPosition())
                                    .mImageLocations((ArrayList<String>) mImageLoc)
                                    .build();

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, itemView, "fullImage");
                            mContext.startActivity(intent, options.toBundle());

                        }
                    }));
                }
            }
        }
    }
}
