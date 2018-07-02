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
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.single_image_view.view.*
import timber.log.Timber
import java.io.File

/**
 * IMAGE ADAPTER TAKES IN LIST OF STRING WHERE EACH
 * STRING REPRESENTS A FILE NAME, IT USES THIS FILENAME TO
 * CREATE A NEW BITMAP FILE AND DISPLAY EACH IMAGE IN ITS
 * RECYCLER VIEW, IT ALSO TAKES A onItemRemoveListener INTERFACE WHICH
 * IS RESPONSIBLE FOR LETTING THE RECYCLER VIEWS ACTIVITY/FRAGMENT
 * KNOW ABOUT THE CHANGE IN SIZE OF THE ADAPTER
 */

class ImageAdapter(private val mFullScreen: Boolean) : ListAdapter<String, ImageAdapter.MyViewHolder>(StringDiffUtil()) {

    private lateinit var glideRequestManager: RequestManager
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        glideRequestManager = (context.applicationContext as MyApplication).requestManager
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.single_image_view, parent, false))
    }

    override fun onBindViewHolder(holder: ImageAdapter.MyViewHolder, position: Int) {
        Timber.d(getItem(position))
        glideRequestManager.load(File((context.applicationContext as MyApplication).rootFolder, getItem(position)))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        Timber.d(e?.message)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        holder.image.visibility = View.VISIBLE
                        holder.progress.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.image)

    }

    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cleanUp()
    }

    inner class MyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.singleImage

        private val remove: ImageView = itemView.singleImageCancel

        val progress: ProgressBar = itemView.singleImageProgress

        init {

            /*
            if the adapter is set for a full screen
            activity/fragment then remove the option
            remove the image or click on image
             */

            if (mFullScreen) {
                remove.visibility = View.GONE
                image.scaleType = ImageView.ScaleType.CENTER
            } else {
                compositeDisposable.addAll(
                        RxView.clicks(remove).subscribe {
                            if (adapterPosition != RecyclerView.NO_POSITION) {

                            }
                        },
                        RxView.clicks(image).subscribe {
                            if (adapterPosition != RecyclerView.NO_POSITION) {

                            }
                        }
                )
            }

        }
    }
}
