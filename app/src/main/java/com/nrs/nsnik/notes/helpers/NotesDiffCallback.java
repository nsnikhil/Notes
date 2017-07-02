package com.nrs.nsnik.notes.helpers;

import android.support.v7.util.DiffUtil;

import com.nrs.nsnik.notes.objects.NoteObject;

import java.util.List;

public class NotesDiffCallback extends DiffUtil.Callback {

    private List<NoteObject> mOldList, mNewList;

    public NotesDiffCallback(List<NoteObject> oldList, List<NoteObject> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }
}
