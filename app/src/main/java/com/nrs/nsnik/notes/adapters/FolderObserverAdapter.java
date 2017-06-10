package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nrs.nsnik.notes.ContainerActivity;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.data.FolderDataObserver;
import com.nrs.nsnik.notes.data.TableNames;
import com.nrs.nsnik.notes.interfaces.FolderCount;
import com.nrs.nsnik.notes.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FolderObserverAdapter extends RecyclerView.Adapter<FolderObserverAdapter.MyViewHolder> implements Observer {

    private static final String[] colorArray = {"#D32F2F", "#C2185B", "#7B1FA2", "#512DA8", "#303F9F", "#1976D2", "#0288D1",
            "#0097A7", "#00796B", "#388E3C", "#689F38", "#AFB42B", "#FBC02D", "#FFA000", "#F57C00", "#E64A19"};
    private static final String TAG = FolderDataObserver.class.getSimpleName();
    private FolderCount mCount;
    private Context mContext;
    private List<String> mFolderList;
    private Random r = new Random();

    public FolderObserverAdapter(Context context, Uri uri,LoaderManager manager, FolderCount count) {
        mContext = context;
        FolderDataObserver observer = new FolderDataObserver(mContext, uri, manager);
        mFolderList = new ArrayList<>();
        mCount = count;
        observer.add(this);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_folder_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mFolderName.setText(mFolderList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFolderList.size();
    }

    public void modifySingle(String object, int position) {
        mFolderList.add(position, object);
        notifyItemChanged(position);
    }

    public void modifyAll(List<String> objects) {
        mFolderList = objects;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mFolderList.remove(position);
        notifyItemChanged(position);
    }

    @Override
    public void updateItems(Cursor cursor) {
        makeFolderList(cursor);
    }

    private void makeFolderList(Cursor cursor) {
        mFolderList.clear();
        while (cursor != null && cursor.moveToNext()) {
            mFolderList.add(justifyName(cursor.getString(cursor.getColumnIndex(TableNames.table2.mFolderName))));
        }
        mCount.getFolderCount(mFolderList.size());
        notifyDataSetChanged();
    }

    private String justifyName(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
    }

    private int getRandom() {
        int color = r.nextInt(colorArray.length);
        return Color.parseColor(colorArray[color]);
    }

    private ColorStateList stateList() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_pressed}
        };
        int color = getRandom();
        int[] colors = new int[]{color, color, color, color};
        return new ColorStateList(states, colors);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleFolderName)
        TextView mFolderName;
        @BindView(R.id.singleFolderIcon)
        ImageView mFolderIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFolderIcon.setImageTintList(stateList());
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,ContainerActivity.class);
                    intent.putExtra(mContext.getResources().getString(R.string.intentFolderName),mFolderName.getText().toString());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
