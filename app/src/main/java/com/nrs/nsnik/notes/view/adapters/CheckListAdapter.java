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
    private final CompositeDisposable mCompositeDisposable;

    public CheckListAdapter(Context context, List<CheckListObject> list, OnAddClickListener onAddClickListener) {
        mContext = context;
        mCheckList = list;
        mOnAddClickListener = onAddClickListener;
        mCompositeDisposable = new CompositeDisposable();
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
        @BindView(R.id.checkListTicker)
        CheckBox mTicker;
        @BindView(R.id.checkListItem)
        EditText mText;
        @BindView(R.id.checkListAdd)
        ImageButton mAdd;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mCompositeDisposable.add(RxTextView.textChanges(mText).subscribe(charSequence -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    CheckListObject checkListObject = mCheckList.get(getAdapterPosition());
                    checkListObject.setmText(charSequence.toString());
                }
            }));
            mCompositeDisposable.add(RxTextView.editorActionEvents(mText).subscribe(textViewEditorActionEvent -> {
                if (textViewEditorActionEvent.actionId() == EditorInfo.IME_ACTION_NEXT) {
                    mOnAddClickListener.addClickListener();
                    mText.requestFocus();
                }
            }));
            mCompositeDisposable.add(RxView.clicks(mAdd).subscribe(v -> mOnAddClickListener.addClickListener()));
            mCompositeDisposable.add(RxCompoundButton.checkedChanges(mTicker).subscribe(aBoolean -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    CheckListObject checkListObject = mCheckList.get(getAdapterPosition());
                    checkListObject.setmDone(aBoolean);
                    if (mText.getText().toString().length() > 0) {
                        changeItem(aBoolean, mText);
                    }
                }
            }));
        }
    }
}
