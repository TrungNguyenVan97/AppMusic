package com.example.appmusicmp3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Tags.ACTION_PLAY_PAUSE)) {
            if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                MusicBuilder.g().pause();
            } else {
                MusicBuilder.g().play();
            }
            Intent intentPlayPause = new Intent(Tags.NOTIFICATION_PLAY_PAUSE);
            context.sendBroadcast(intentPlayPause);
        }

        if (action.equals(Tags.ACTION_NEXT)) {
            MusicBuilder.g().stop();
            MusicBuilder.g().nextSong();
            MusicBuilder.g().initMediaPlayer(context, MusicBuilder.g().getSongPlaying());
            MusicBuilder.g().play();

            Intent intentNext = new Intent(Tags.NOTIFICATION_NEXT);
            context.sendBroadcast(intentNext);
        }

        if (action.equals(Tags.ACTION_PREV)) {
            MusicBuilder.g().stop();
            MusicBuilder.g().preSong();
            MusicBuilder.g().initMediaPlayer(context, MusicBuilder.g().getSongPlaying());
            MusicBuilder.g().play();

            Intent intentPrev = new Intent(Tags.NOTIFICATION_PREV);
            context.sendBroadcast(intentPrev);
        }
    }
}
