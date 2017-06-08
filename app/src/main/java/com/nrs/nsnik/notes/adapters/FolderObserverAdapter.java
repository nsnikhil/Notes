package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FolderObserverAdapter extends RecyclerView.Adapter<FolderObserverAdapter.MyViewHolder> implements Observer {

    private Context mContext;
    private List<String> mFolderList;
    private FolderDataObserver observer;

    public FolderObserverAdapter(Context context, LoaderManager manager){
        mContext = context;
        observer = new FolderDataObserver(mContext,manager);
        mFolderList  = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_folder_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mFolderName.setText(mFolderList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFolderList.size();
    }

    public void modifySingle(String object,int position){
        mFolderList.add(position,object);
        notifyItemChanged(position);
    }

    public void modifyAll(List<String> objects){
        mFolderList = objects;
        notifyDataSetChanged();
    }

    public void removeItem(int position){
        mFolderList.remove(position);
        notifyItemChanged(position);
    }

    @Override
    public void updateItems(Cursor cursor) {
        makeFolderList(cursor);
    }

    private void makeFolderList(Cursor cursor){
        mFolderList.clear();
        while (cursor!=null&&cursor.moveToNext()){
            mFolderList.add(cursor.getString(cursor.getColumnIndex(TableNames.table2.mFolderName)));
        }
        notifyDataSetChanged();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleFolderName)TextView mFolderName;
        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
