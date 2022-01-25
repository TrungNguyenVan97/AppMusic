package com.example.appmusicmp3;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogRecord;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private ArrayList<Song> mDataSet;
    private ArrayList<Song> listSearch = MusicBuilder.g().getListSong();
    private CallBack callBack = null;

    public SongAdapter(ArrayList<Song> mDataSet) {
        if (mDataSet != null) {
            this.mDataSet = mDataSet;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new SongHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SongHolder) {
            ((SongHolder) holder).onBind(mDataSet.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<Song> list = new ArrayList();
                String strSearch = constraint.toString();
                if (strSearch.isEmpty()) {
                    list.addAll(listSearch);
                } else {
                    for (int i = 0; i < listSearch.size(); i++) {
                        Song song = listSearch.get(i);
                        if (song.getTitle().toUpperCase().contains(strSearch.toUpperCase()) || song.getArtist().toUpperCase().contains(strSearch.toUpperCase())) {
                            list.add(listSearch.get(i));
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = list;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                reset((List<Song>) results.values);
            }
        };
    }

    public void reset(List<Song> listSong) {
        Log.d("aaa", "reset");
        this.mDataSet.clear();
        if (listSong != null) {
            mDataSet.addAll(listSong);
        }
        notifyDataSetChanged();
    }

    public class SongHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private TextView tvArtist;
        private ImageView imgAvatar;
        private LinearLayout layoutSong;

        public SongHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTitle.setSelected(true);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            layoutSong = itemView.findViewById(R.id.layoutSong);
            onClick();
        }

        private void onBind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
        }

        private void onClick() {
            layoutSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // getAdapterPosition() đôi khi sẽ trả về giá trị NO_POSITION
                    try {
                        if (callBack != null) {
                            callBack.playMP3(getAdapterPosition());
                        }
                    } catch (Exception e) {

                    }
                }
            });
        }
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    interface CallBack {
        void playMP3(int position);
    }
}
