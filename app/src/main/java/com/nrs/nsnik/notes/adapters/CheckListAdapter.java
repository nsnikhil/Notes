package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

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
    }

    public void modifyCheckList(List<CheckListObject> list) {
        mCheckList = list;
        notifyDataSetChanged();
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
        }
    }
}
