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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.interfaces.OnColorSelectedListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mColorList;
    private OnColorSelectedListener mColorSelectedListener;

    public ColorPickerAdapter(Context context, List<String> list, OnColorSelectedListener onColorSelectedListener) {
        mContext = context;
        mColorList = list;
        mColorSelectedListener = onColorSelectedListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.color_picker_image, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mColor.setImageDrawable(new ColorDrawable(Color.parseColor(mColorList.get(position))));
    }

    @Override
    public int getItemCount() {
        return mColorList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleColor)
        ImageView mColor;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mColor.setOnClickListener(view -> mColorSelectedListener.onColorSelected(mColorList.get(getAdapterPosition())));
        }
    }
}
