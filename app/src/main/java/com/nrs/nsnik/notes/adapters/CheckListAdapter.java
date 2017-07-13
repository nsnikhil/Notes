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
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.interfaces.OnAddClickListener;
import com.nrs.nsnik.notes.objects.CheckListObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.MyViewHolder> {

    private Context mContext;
    private List<CheckListObject> mCheckList;
    private OnAddClickListener mOnAddClickListener;

    public CheckListAdapter(Context context, List<CheckListObject> list, OnAddClickListener onAddClickListener) {
        mContext = context;
        mCheckList = list;
        mOnAddClickListener = onAddClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_check_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (position == mCheckList.size() - 1) {
            holder.mAdd.setVisibility(View.VISIBLE);
        } else {
            holder.mAdd.setVisibility(View.GONE);
        }
        holder.mText.setText(mCheckList.get(position).getmText());
        holder.mTicker.setChecked(mCheckList.get(position).ismDone());
        if (holder.mText.getText().toString().length() > 0) {
            changeItem(mCheckList.get(position).ismDone(), holder.mText);
        }
    }

    private void changeItem(boolean isDone, TextView textView) {
        if (isDone) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(ContextCompat.getColor(mContext, R.color.grey));
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(ContextCompat.getColor(mContext, android.R.color.primary_text_light));
        }
    }

    @Override
    public int getItemCount() {
        return mCheckList != null ? mCheckList.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.checkListTicker)
        CheckBox mTicker;
        @BindView(R.id.checkListItem)
        EditText mText;
        @BindView(R.id.checkListAdd)
        ImageButton mAdd;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mAdd.setOnClickListener(view -> mOnAddClickListener.addClickListener());
            mTicker.setOnCheckedChangeListener((compoundButton, b) -> {
                CheckListObject checkListObject = mCheckList.get(getAdapterPosition());
                checkListObject.setmDone(b);
                if (mText.getText().toString().length() > 0) {
                    changeItem(b, mText);
                }
            });
            mText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    CheckListObject checkListObject = mCheckList.get(getAdapterPosition());
                    checkListObject.setmText(editable.toString());
                }
            });
        }
    }
}
