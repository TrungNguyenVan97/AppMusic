package com.example.appmusicmp3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Song> mDataSet = new ArrayList<>();
    private CallBack callBack = null;

    public SongAdapter(List<Song> mDataSet) {
        if (mDataSet != null) {
            this.mDataSet = (ArrayList<Song>) mDataSet;
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

    public class SongHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private TextView tvArtist;
        private ConstraintLayout layoutSong;

        public SongHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
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
