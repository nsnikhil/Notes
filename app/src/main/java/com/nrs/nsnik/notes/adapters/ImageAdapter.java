package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nrs.nsnik.notes.ImageFullActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.interfaces.SendSize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private static final String TAG = ImageAdapter.class.getSimpleName();
    private Activity mContext;
    private ArrayList<String> mImageLoc;
    private File mFolder;
    private SendSize mSize;
    private boolean mFullScreen;

    public ImageAdapter(Activity c, ArrayList<String> imageLocations, SendSize sz,boolean forFullScreen) {
        mContext = c;
        mImageLoc  = imageLocations;
        mSize = sz;
        mFullScreen = forFullScreen;
        mFolder =  mContext.getExternalFilesDir(mContext.getResources().getString(R.string.folderName));
    }

    @Override
    public ImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.single_image_view, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.MyViewHolder holder, int position) {
        GetImage image = new GetImage(holder);
        if (mImageLoc != null) {
            image.execute(mImageLoc.get(position));
        }
    }

    private class GetImage extends AsyncTask<String,Void,Bitmap>{

        ImageAdapter.MyViewHolder mHolder;

        GetImage(ImageAdapter.MyViewHolder holder){
            mHolder  = holder;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            return BitmapFactory.decodeFile(new File(mFolder,strings[0]).toString());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mHolder.image.setImageBitmap(bitmap);
            mHolder.mProgress.setVisibility(View.GONE);
        }
    }

    public void modifyList(ArrayList<String> imageLoc){
        mImageLoc = imageLoc;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mImageLoc.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleImage) ImageView image;
        @BindView(R.id.singleImageCancel) ImageView remove;
        @BindView(R.id.singleImageProgress)ProgressBar mProgress;
        public MyViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if(mFullScreen){
                remove.setVisibility(View.GONE);
                image.setScaleType(ImageView.ScaleType.CENTER);
            }else {
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        mImageLoc.remove(pos);
                        notifyItemRemoved(pos);
                        mSize.validateSize(pos);
                    }
                });
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent fullScreen = new Intent(mContext, ImageFullActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList(mContext.getResources().getString(R.string.bundleStringImageArray), mImageLoc);
                        bundle.putInt(mContext.getResources().getString(R.string.bundleArrayListPosition), getAdapterPosition());
                        fullScreen.putExtra(mContext.getResources().getString(R.string.bundleIntentImage), bundle);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(mContext, itemView, "fullImage");
                        mContext.startActivity(fullScreen, options.toBundle());
                    }
                });
            }
        }
    }
}
