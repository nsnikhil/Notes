package com.nrs.nsnik.notes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.nrs.nsnik.notes.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mAudioLocationList;

    public AudioListAdapter(Context context, List<String> list) {
        mContext = context;
        mAudioLocationList = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.single_audio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }

    public void modifyAudioList(List<String> list) {
        mAudioLocationList.addAll(list);
        notifyDataSetChanged();
    }

    private void playAudio() {

    }

    private void removeAudio() {

    }

    @Override
    public int getItemCount() {
        return mAudioLocationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.audioProgressBar)
        SeekBar mSeekBar;
        @BindView(R.id.audioPlay)
        ImageButton mPlay;
        @BindView(R.id.audioRemove)
        ImageButton mRemove;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mPlay.setOnClickListener(view -> playAudio());
            mRemove.setOnClickListener(view -> removeAudio());
        }
    }
}
