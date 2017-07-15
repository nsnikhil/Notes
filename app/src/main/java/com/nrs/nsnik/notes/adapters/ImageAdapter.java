/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nrs.nsnik.notes.ImageFullActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.helpers.FileOperation;
import com.nrs.nsnik.notes.interfaces.OnItemRemoveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
IMAGE ADAPTER TAKES IN LIST OF STRING WHERE EACH
 STRING REPRESENTS A FILE NAME, IT USES THIS FILENAME TO
 CREATE A NEW BITMAP FILE AND DISPLAY EACH IMAGE IN ITS
 RECYCLER VIEW, IT ALSO TAKES A SENDSIZE INTERFACE WHICH
 IS RESPONSIBLE FOR LETTING THE RECYCLER VIEWS ACTIVITY/FRAGMENT
 KNOW ABOUT THE CHANGE IN SIZE OF THE ADAPTER
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private static final String TAG = ImageAdapter.class.getSimpleName();
    private Activity mContext;
    private List<String> mImageLoc;
    private File mFolder;
    private OnItemRemoveListener mOnItemRemoveListener;
    private boolean mFullScreen;

    /*
     @param c               the context object
     @param imageLocation   list of image file names
     @param sz              SendSize interface
     @param forFullScreen   if the adapter is used in full screen or not
     */
    public ImageAdapter(Activity c, List<String> imageLocations, OnItemRemoveListener onItemRemoveListener, boolean forFullScreen) {
        mContext = c;
        mImageLoc = imageLocations;
        mOnItemRemoveListener = onItemRemoveListener;
        mFullScreen = forFullScreen;
        mFolder = mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
    }

    @Override
    public ImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_image_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final ImageAdapter.MyViewHolder holder, int position) {
        Glide.with(mContext)
                .load(new File(mFolder, mImageLoc.get(position)).toString())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.image.setVisibility(View.VISIBLE);
                        holder.mProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        if (mImageLoc != null) {
            return mImageLoc.size();
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleImage)
        ImageView image;
        @BindView(R.id.singleImageCancel)
        ImageView remove;
        @BindView(R.id.singleImageProgress)
        ProgressBar mProgress;

        public MyViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            /*
            if the adapter is set for a full screen
            activity/fragment then remove the option
            remove the image or click on image
             */
            if (mFullScreen) {
                remove.setVisibility(View.GONE);
                image.setScaleType(ImageView.ScaleType.CENTER);
            } else {
                remove.setOnClickListener(view -> mOnItemRemoveListener.onItemRemoved(getAdapterPosition(), FileOperation.FILE_TYPES.IMAGE, mImageLoc.get(getAdapterPosition())));
                image.setOnClickListener(view -> {
                    Intent fullScreen = new Intent(mContext, ImageFullActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(mContext.getResources().getString(R.string.bundleStringImageArray), (ArrayList<String>) mImageLoc);
                    bundle.putInt(mContext.getResources().getString(R.string.bundleArrayListPosition), getAdapterPosition());
                    fullScreen.putExtra(mContext.getResources().getString(R.string.bundleIntentImage), bundle);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext, itemView, "fullImage");
                    mContext.startActivity(fullScreen, options.toBundle());
                });
            }
        }
    }
}
