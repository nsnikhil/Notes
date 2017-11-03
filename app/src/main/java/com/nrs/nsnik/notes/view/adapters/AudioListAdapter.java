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

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.MyApplication;
import com.nrs.nsnik.notes.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;


public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.MyViewHolder> implements Runnable {

    private final Context mContext;
    private final List<String> mAudioLocationList;
    @NonNull
    private final CompositeDisposable mCompositeDisposable;
    @Nullable
    private MediaPlayer mMediaPlayer;
    @Nullable
    private SeekBar mAdapterSeekBar;

    public AudioListAdapter(Context context, List<String> list) {
        mContext = context;
        mAudioLocationList = list;
        //mMediaPlayer = DaggerMediaComponent.builder().build().getMediaPlayer();
        mCompositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_audio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
    }

    private void playAudio(int position, @NonNull ImageButton play) {
        Timber.d(String.valueOf(new File(((MyApplication) mContext.getApplicationContext()).getRootFolder(), mAudioLocationList.get(position))));
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.setDataSource(String.valueOf(new File(((MyApplication) mContext.getApplicationContext()).getRootFolder(), mAudioLocationList.get(position))));
                mMediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    if (mAdapterSeekBar != null) {
                        mAdapterSeekBar.setMax(mMediaPlayer.getDuration());
                    }
                    new Thread(this).start();
                }
            });
            mMediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                }
                if (mAdapterSeekBar != null) {
                    mAdapterSeekBar.setProgress(0, true);
                }
                play.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_black_24px));
            });
        }
    }

    @Override
    public int getItemCount() {
        return mAudioLocationList.size();
    }

    @Override
    public void run() {
        int currentPosition;
        if (mMediaPlayer != null) {
            currentPosition = mMediaPlayer.getCurrentPosition();
            int total = mMediaPlayer.getDuration();
            while (mMediaPlayer != null && currentPosition < total) {
                try {
                    Thread.sleep(1000);
                    currentPosition = mMediaPlayer.getCurrentPosition();
                } catch (Exception e) {
                    return;
                }
                if (mAdapterSeekBar != null) {
                    mAdapterSeekBar.setProgress(currentPosition, true);
                }
            }
        }
    }

    private void cleanUp() {
        mCompositeDisposable.clear();
        mCompositeDisposable.dispose();
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        cleanUp();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.audioProgressBar)
        SeekBar mSeekBar;
        @Nullable
        @BindView(R.id.audioPlay)
        ImageButton mPlay;
        @Nullable
        @BindView(R.id.audioRemove)
        ImageButton mRemove;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mPlay != null) {
                mCompositeDisposable.add(RxView.clicks(mPlay).subscribe(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mAdapterSeekBar = mSeekBar;
                        mPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_stop_black_24px));
                        playAudio(getAdapterPosition(), mPlay);
                    }
                }));
            }
            if (mRemove != null) {
                mCompositeDisposable.add(RxView.clicks(mRemove).subscribe(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        //mOnItemRemoveListener.onItemRemoved(getAdapterPosition(), FileUtil.FILE_TYPES.AUDIO, mAudioLocationList.get(getAdapterPosition()));
                    }
                }));
            }
        }
    }
}
