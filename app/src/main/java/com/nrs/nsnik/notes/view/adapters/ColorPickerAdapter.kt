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

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.util.events.ColorPickerEvent
import com.nrs.nsnik.notes.view.adapters.diffUtil.StringDiffUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.color_picker_image.view.*
import org.greenrobot.eventbus.EventBus


class ColorPickerAdapter : ListAdapter<String, ColorPickerAdapter.MyViewHolder>(StringDiffUtil()) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.color_picker_image, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.color.setImageDrawable(ColorDrawable(Color.parseColor(getItem(position))))
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
        val color: ImageView = itemView.singleColor

        init {
            compositeDisposable.add(RxView.clicks(color).subscribe {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    EventBus.getDefault().post(ColorPickerEvent(getItem(adapterPosition)))
                }
            })
        }
    }
}
