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
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.objects.CheckListObject;
import com.nrs.nsnik.notes.util.interfaces.OnAddClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.MyViewHolder> {

    private final Context mContext;
    private final OnAddClickListener mOnAddClickListener;
    private final List<CheckListObject> mCheckList;
    @NonNull
    private final CompositeDisposable mCompositeDisposable;

    public CheckListAdapter(Context context, List<CheckListObject> list, OnAddClickListener onAddClickListener) {
        mContext = context;
        mCheckList = list;
        mOnAddClickListener = onAddClickListener;
        mCompositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_check_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CheckListObject object = mCheckList.get(position);
        if (holder.mAdd != null) {
            if (position == mCheckList.size() - 1) {
                holder.mAdd.setVisibility(View.VISIBLE);
            } else {
                holder.mAdd.setVisibility(View.GONE);
            }
        }
        if (holder.mText != null) {
            holder.mText.setText(object.text());
        }
        if (holder.mTicker != null && holder.mText != null) {
            holder.mTicker.setChecked(object.done());
            if (holder.mText.getText().toString().length() > 0) {
                changeItem(object.done(), holder.mText);
            }
        }
    }

    private void changeItem(boolean isDone, @NonNull TextView textView) {
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

    private void cleanUp() {
        mCompositeDisposable.clear();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        cleanUp();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.checkListTicker)
        CheckBox mTicker;
        @Nullable
        @BindView(R.id.checkListItem)
        EditText mText;
        @Nullable
        @BindView(R.id.checkListAdd)
        ImageButton mAdd;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mText != null && mTicker != null) {
                mCompositeDisposable.add(RxTextView.textChanges(mText).subscribe(charSequence -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mCheckList.set(getAdapterPosition(), CheckListObject.builder()
                                .text(charSequence.toString())
                                .done(mTicker.isChecked())
                                .build());
                    }
                }));
                mCompositeDisposable.add(RxTextView.editorActionEvents(mText).subscribe(textViewEditorActionEvent -> {
                    if (textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_NEXT) {
                        mOnAddClickListener.addClickListener();
                        mText.requestFocus();
                    }
                }));
            }
            if (mAdd != null) {
                mCompositeDisposable.add(RxView.clicks(mAdd).subscribe(v -> mOnAddClickListener.addClickListener()));
            }
            if (mTicker != null) {
                mCompositeDisposable.add(RxCompoundButton.checkedChanges(mTicker).subscribe(aBoolean -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mCheckList.set(getAdapterPosition(), CheckListObject.builder()
                                .text(mText.getText().toString())
                                .done(aBoolean)
                                .build());
                        if (mText.getText().toString().length() > 0) {
                            changeItem(aBoolean, mText);
                        }
                    }
                }));
            }
        }
    }
}
