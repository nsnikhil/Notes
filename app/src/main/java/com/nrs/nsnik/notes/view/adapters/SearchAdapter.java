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
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.model.objects.SearchObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

/*
this adapter takes of list of search object which
contains two fields, first the name of and second a
boolean that specifies if the item is folder
or note
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private final Context mContext;
    @NonNull
    private final CompositeDisposable mCompositeDisposable;
    private List<SearchObject> mSearchList;

    /**
     * @param context    the context object
     * @param searchList the list of search objects
     */
    public SearchAdapter(Context context, List<SearchObject> searchList) {
        mContext = context;
        mSearchList = searchList;
        mCompositeDisposable = new CompositeDisposable();
    }

    @NonNull
    @Override
    public SearchAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        /*
        binder checks if the item at position
        has true for its isFolder field and
        appropriately attaches the icon to text
         */
        if (holder.mSearchName != null) {
            if (mSearchList.get(position).isFolder()) {
                holder.mSearchName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_folder_black_48px, 0, 0, 0);
            } else {
                holder.mSearchName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_note_black_48px, 0, 0, 0);
            }
            holder.mSearchName.setCompoundDrawableTintList(stateList());
            holder.mSearchName.setText(mSearchList.get(position).name());
        }
    }

    /**
     * @return new ColorStateList
     */
    @NonNull
    private ColorStateList stateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_pressed}
        };
        int color = ContextCompat.getColor(mContext, R.color.colorAccentLight);
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    @Override
    public int getItemCount() {
        return mSearchList.size();
    }

    /**
     * called to change the content of searchable list
     * and notify the adapter
     * <p>
     * TODO REPLACE NOTIFYSETDATA CHANGE WITH DIFF UTIL
     */
    public void modifyList(List<SearchObject> list) {
        mSearchList = list;
        notifyDataSetChanged();
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
        @BindView(R.id.searchItemName)
        TextView mSearchName;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mCompositeDisposable.add(RxView.clicks(itemView).subscribe(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    SearchObject object = mSearchList.get(getAdapterPosition());
                    if (object.isFolder()) {
                        // Intent folderIntent = new Intent(mContext, ContainerActivity.class);
                        Toast.makeText(mContext, "Will open folder", Toast.LENGTH_LONG).show();
                        //mContext.startActivity(folderIntent);
                    } else {
                        //  Intent noteIntent = new Intent(mContext, NewNoteActivity.class);
                        Toast.makeText(mContext, "Will open note", Toast.LENGTH_LONG).show();
                        //mContext.startActivity(noteIntent);
                    }
                }
            }));
        }
    }
}
