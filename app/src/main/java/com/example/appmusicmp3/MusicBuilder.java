package com.example.appmusicmp3;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MusicBuilder {
    private static MusicBuilder instance;
    private static MediaPlayer mediaPlayer;

    public static MusicBuilder g() {
        if (instance == null) {
            instance = new MusicBuilder();
        }
        return instance;
    }

    public void initMediaPlayer(Context context, Uri uri){
        mediaPlayer = MediaPlayer.create(context,uri);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
