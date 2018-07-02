/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.dagger.components.DaggerMediaComponent
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
import com.nrs.nsnik.notes.view.listeners.AdapterType
import com.nrs.nsnik.notes.view.listeners.OnItemRemoveListener
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.single_audio_item.view.*
import java.io.File
import java.io.IOException


class AudioListAdapter(private val onItemRemoveListener: OnItemRemoveListener) : ListAdapter<String, AudioListAdapter.MyViewHolder>(StringDiffUtil()), Runnable {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context
    private var mMediaPlayer: MediaPlayer? = DaggerMediaComponent.builder().build().mediaPlayer
    private var mAdapterSeekBar: SeekBar? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_audio_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {}

    private fun playAudio(position: Int, play: ImageButton) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer!!.setDataSource(File((context.applicationContext as MyApplication).rootFolder, getItem(position)).toString())
                mMediaPlayer!!.prepareAsync()

            } catch (e: IOException) {
                e.printStackTrace()
            }

            mMediaPlayer!!.setOnPreparedListener {
                if (mMediaPlayer != null && !mMediaPlayer!!.isPlaying) {
                    mMediaPlayer!!.start()
                    if (mAdapterSeekBar != null) {
                        mAdapterSeekBar!!.max = mMediaPlayer!!.duration
                    }
                    Thread(this).start()
                }
            }
            mMediaPlayer!!.setOnCompletionListener {
                if (mMediaPlayer != null) {
                    mMediaPlayer!!.reset()
                }
                if (mAdapterSeekBar != null) {
                    mAdapterSeekBar!!.setProgress(0, true)
                }
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_24px))
            }
        }
    }

    override fun run() {
        var currentPosition: Int
        if (mMediaPlayer != null) {
            currentPosition = mMediaPlayer!!.currentPosition
            val total = mMediaPlayer!!.duration
            while (mMediaPlayer != null && currentPosition < total) {
                try {
                    Thread.sleep(1000)
                    currentPosition = mMediaPlayer!!.currentPosition
                } catch (e: Exception) {
                    return
                }

                if (mAdapterSeekBar != null) {
                    mAdapterSeekBar!!.setProgress(currentPosition, true)
                }
            }
        }
    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cleanUp()
    }

    inner class MyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val seekBar: SeekBar = itemView.audioProgressBar
        private val play: ImageButton = itemView.audioPlay
        private val remove: ImageButton = itemView.audioRemove

        init {

            compositeDisposable.addAll(
                    RxView.clicks(play).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            mAdapterSeekBar = seekBar
                            play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_stop_black_24px))
                            playAudio(adapterPosition, play)
                        }
                    },
                    RxView.clicks(remove).subscribe {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onItemRemoveListener.onItemRemoved(adapterPosition,AdapterType.AUDIO_ADAPTER)
                            //mOnItemRemoveListener.onItemRemoved(getAdapterPosition(), FileUtil.FILE_TYPES.AUDIO, mAudioLocationList.get(getAdapterPosition()));
                        }
                    }
            )
        }
    }
}
