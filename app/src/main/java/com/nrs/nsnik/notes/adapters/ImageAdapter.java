package com.nrs.nsnik.notes.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.nrs.nsnik.notes.R;
import com.nrs.nsnik.notes.interfaces.SendSize;

import java.util.ArrayList;

import butterknife.BindView;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    private Activity mContext;
    private ArrayList<Bitmap> list;
    private SendSize size;

    public ImageAdapter(Activity c, ArrayList<Bitmap> arrayList, SendSize sz) {
        mContext = c;
        list = arrayList;
        size = sz;
    }

    @Override
    public ImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.single_image_view, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.MyViewHolder holder, int position) {
        holder.image.setImageBitmap(list.get(position));

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.singleImage) ImageView image;
        @BindView(R.id.singleImageCancel) CircularImageView remove;

        public MyViewHolder(View itemView) {
            super(itemView);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    size.validateSize(getAdapterPosition());
                }
            });image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext.getApplicationContext(), "Will Show Full Screen Image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}