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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.dagger.components.DaggerMediaComponent;
import com.nrs.nsnik.notes.util.FileOperation;
import com.nrs.nsnik.notes.util.interfaces.OnItemRemoveListener;
import com.nrs.nsnik.notes.view.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.MyViewHolder> implements Runnable {

    private final Context mContext;
    private final OnItemRemoveListener mOnItemRemoveListener;
    private MediaPlayer mMediaPlayer;
    private List<String> mAudioLocationList;
    private SeekBar mAdapterSeekBar;

    public AudioListAdapter(Context context, List<String> list, OnItemRemoveListener onItemRemoveListener) {
        mContext = context;
        mAudioLocationList = list;
        mOnItemRemoveListener = onItemRemoveListener;
        mMediaPlayer = DaggerMediaComponent.builder().build().getMediaPlayer();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_audio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
    }

    private void playAudio(int position, ImageButton play) {
        Timber.d(String.valueOf(new File(((MyApplication) mContext.getApplicationContext()).getRootFolder(), mAudioLocationList.get(position))));
        try {
            mMediaPlayer.setDataSource(String.valueOf(new File(((MyApplication) mContext.getApplicationContext()).getRootFolder(), mAudioLocationList.get(position))));
            if (mMediaPlayer != null) {
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mAdapterSeekBar.setMax(mMediaPlayer.getDuration());
                new Thread(this).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
                mAdapterSeekBar.setProgress(0, true);
                play.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_black_24dp));
            }
        });
    }


    @Override
    public int getItemCount() {
        return mAudioLocationList.size();
    }

    @Override
    public void run() {
        int currentPosition = mMediaPlayer.getCurrentPosition();
        int total = mMediaPlayer.getDuration();
        while (mMediaPlayer != null && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mMediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                return;
            }
            mAdapterSeekBar.setProgress(currentPosition, true);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.audioProgressBar)
        SeekBar mSeekBar;
        @BindView(R.id.audioPlay)
        ImageButton mPlay;
        @BindView(R.id.audioRemove)
        ImageButton mRemove;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mPlay.setOnClickListener(view -> {
                mAdapterSeekBar = mSeekBar;
                mPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_stop_black_24dp));
                playAudio(getAdapterPosition(), mPlay);
            });
            mRemove.setOnClickListener(view -> mOnItemRemoveListener.onItemRemoved(getAdapterPosition(), FileOperation.FILE_TYPES.AUDIO, mAudioLocationList.get(getAdapterPosition())));
        }
    }
}
