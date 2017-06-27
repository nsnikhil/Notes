package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private static final String TAG = ImageAdapter.class.getSimpleName();
    private Activity mContext;
    private ArrayList<String> mImageLoc;
    private File mFolder;
    private SendSize mSize;
    private boolean mFullScreen;
    CompositeDisposable mDisposable;

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
        setImage(position,holder);
    }

    private void testMethod(){
        PublishSubject<Bitmap> subject = PublishSubject.create();
        subject.subscribe(new Observer<Bitmap>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Bitmap bitmap) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        Single.just(5).map(new Function<Integer, String>() {
            @Override
            public String apply(@NonNull Integer integer) throws Exception {
                return null;
            }
        });
    }

    private void setImage(final int position, final ImageAdapter.MyViewHolder holder){
        Single<Bitmap>  singleObservable = Single.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return BitmapFactory.decodeFile(new File(mFolder,mImageLoc.get(position)).toString());
            }
        });
        singleObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Bitmap>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d(TAG, d.toString());
                    }

                    @Override
                    public void onSuccess(@NonNull Bitmap bitmap) {
                        holder.image.setImageBitmap(bitmap);
                        holder.mProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
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
                        mContext.startActivity(fullScreen);
                    }
                });
            }
        }
    }
}
