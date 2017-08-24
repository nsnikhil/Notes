/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.fragments.dialogFragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.util.interfaces.OnColorSelectedListener;
import com.nrs.nsnik.notes.view.adapters.ColorPickerAdapter;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ColorPickerDialogFragment extends DialogFragment implements OnColorSelectedListener {

    @Nullable
    @BindView(R.id.colorPickerList)
    RecyclerView mColorList;
    private OnColorSelectedListener mOnColorSelectedListener;

    public ColorPickerDialogFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color_picker_dialog, container, false);
        ButterKnife.bind(this, v);
        initialize();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() != null) {
            try {
                mOnColorSelectedListener = (OnColorSelectedListener) getTargetFragment();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mOnColorSelectedListener = (OnColorSelectedListener) context;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize() {
        List<String> colorList = Arrays.asList(getActivity().getResources().getStringArray(R.array.backgroundColors));
        if (mColorList != null) {
            mColorList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        }
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getActivity(), colorList, this);
        mColorList.setAdapter(colorPickerAdapter);
    }

    @Override
    public void onColorSelected(String color) {
        mOnColorSelectedListener.onColorSelected(color);
        dismiss();
    }
}
