package com.nrs.nsnik.notes.view.fragments.dialogFragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.view.RxView;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderEntity;
import com.nrs.nsnik.notes.util.events.ColorPickerEvent;
import com.nrs.nsnik.notes.viewmodel.FolderViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;


public class CreateFolderDialog extends DialogFragment {

    @BindView(R.id.dialogFolderName)
    EditText mFolderName;
    @BindView(R.id.dialogFolderColor)
    ImageView mFolderColor;
    @BindView(R.id.dialogFolderCreate)
    Button mCreate;
    @BindView(R.id.dialogFolderCancel)
    Button mCancel;
    private Unbinder mUnbinder;
    private CompositeDisposable mCompositeDisposable;
    private String mColor = "#212121";
    private FolderViewModel mFolderViewModel;
    private String mParentFolderName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_folder_dialog, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        initialize();
        listeners();
        return v;
    }

    private void initialize() {
        mCompositeDisposable = new CompositeDisposable();
        mFolderViewModel = ViewModelProviders.of(this).get(FolderViewModel.class);
        if (getActivity() != null && getArguments() != null) {
            mParentFolderName = getArguments().getString(getActivity().getResources().getString(R.string.bundleCreateFolderParentFolder));
        }
    }

    private void listeners() {
        mCompositeDisposable.add(RxView.clicks(mFolderColor).subscribe(o -> {
            ColorPickerDialogFragment pickerDialogFragment = new ColorPickerDialogFragment();
            if (getFragmentManager() != null) {
                pickerDialogFragment.show(getFragmentManager(), "color");
            }
        }));
        mCompositeDisposable.add(RxView.clicks(mCreate).subscribe(o -> {
            createFolder();
        }));
        mCompositeDisposable.add(RxView.clicks(mCancel).subscribe(o -> {
            dismiss();
        }));
    }

    private void createFolder() {
        if (!mFolderName.getText().toString().isEmpty()) {
            FolderEntity folderEntity = new FolderEntity();
            folderEntity.setFolderName(mFolderName.getText().toString());
            folderEntity.setColor(mColor);
            folderEntity.setIsLocked(0);
            folderEntity.setIsPinned(0);
            folderEntity.setParentFolderName(mParentFolderName);
            mFolderViewModel.insertFolder(folderEntity);
            dismiss();
        } else {
            if (getActivity() != null) {
                mFolderName.setError(getActivity().getResources().getString(R.string.errorNoFolderName));
            }
        }
    }

    private void cleanUp() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onColorPickerEvent(ColorPickerEvent colorPickerEvent) {
        mColor = colorPickerEvent.getColor();
        mFolderColor.setBackgroundTintList(stateList(mColor));
    }

    @NonNull
    private ColorStateList stateList(String colorString) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_pressed}
        };
        int color = Color.parseColor(colorString);
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }
}
