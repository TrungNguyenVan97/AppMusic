package com.example.appmusicmp3;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class MusicBuilder {
    private static int PAUSE = 0;
    private static int PLAYING = 1;
    private static int STOP = 3;
    private static MusicBuilder instance;
    private static MediaPlayer mediaPlayer;
    private int status = 0;

    public static MusicBuilder g() {
        if (instance == null) {
            instance = new MusicBuilder();
        }
        return instance;
    }

    public void initMediaPlayer(Context context, Uri uri) {
        stop();
        mediaPlayer = MediaPlayer.create(context, uri);
        status = STOP;
    }

    public void play() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.start();
            status = PLAYING;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.pause();
            status = PAUSE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            status = STOP;
        } catch (Exception e) {

        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
