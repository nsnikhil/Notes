package com.nrs.nsnik.notes.fragments.dialogFragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.adapters.ColorPickerAdapter;
import com.nrs.nsnik.notes.interfaces.OnColorSelectedListener;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class ColorPickerDialogFragment extends DialogFragment implements OnColorSelectedListener {

    @BindView(R.id.colorPickerList)
    RecyclerView mColorList;
    private ColorPickerAdapter mColorPickerAdapter;

    public ColorPickerDialogFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color_picker_dialog, container, false);
        ButterKnife.bind(this, v);
        initialize();
        return v;
    }


    private void initialize() {
        List<String> colorList = Arrays.asList(getActivity().getResources().getStringArray(R.array.backgroundColors));
        mColorList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        mColorPickerAdapter = new ColorPickerAdapter(getActivity(), colorList, this);
        mColorList.setAdapter(mColorPickerAdapter);
    }

    @Override
    public void onColorSelected(String color) {
        Timber.d(color);
        dismiss();
    }
}
