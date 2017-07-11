package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.objects.CheckListObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.MyViewHolder> {

    private Context mContext;
    private List<CheckListObject> mCheckList;

    public CheckListAdapter(Context context, List<CheckListObject> list) {
        mContext = context;
        mCheckList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_check_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    public void modifyCheckList(List<CheckListObject> list) {
        mCheckList.addAll(list);
    }

    @Override
    public int getItemCount() {
        return mCheckList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.checkListTicker)
        CheckBox mTicker;
        @BindView(R.id.checkListItem)
        EditText mText;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
