package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.graphics.Color;
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
        holder.mColor.setBackgroundColor(Color.parseColor(mColorList.get(position)));
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
            mColor.setOnClickListener(view -> {
                mColorSelectedListener.onColorSelected(mColorList.get(getAdapterPosition()));
            });
        }
    }
}
