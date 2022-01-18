package com.example.appmusicmp3;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.nio.file.ProviderNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongService extends Service {

    private static final int ID_FOREGROUND = 2025;
    private RemoteViews remoteViews;
    private static final int ACTION_PLAY_PAUSE = 1111;
    private static final int ACTION_NEXT = 1112;
    private static final int ACTION_PREV = 1113;
    private static final int ACTION_RANDOM = 1114;
    private static final int ACTION_REPEAT = 1115;
    public static final String SEND_SONG_TO_RECEIVER = "SEND_SONG_TO_RECEIVER";
    public static final String ACTION_SEND_SONG_RECEIVER = "ACTION_SEND_SONG_RECEIVER";
    private Song songPlaying ;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        songPlaying = (Song) intent.getSerializableExtra(MainActivity.EXTRA_SERVICE_SONG);

        initMedia();
        autoNextSong();
        sendNotification(songPlaying);

        return START_NOT_STICKY;
    }

    public void initMedia() {
        Log.d("check", "initMedia");
        setData();
        MusicBuilder.g().play();
    }

    private void setData() {
        Log.d("check", "setDataS");
        if (songPlaying == null) {
            return;
        }
        Uri uri = Uri.parse(songPlaying.getData());
        MusicBuilder.g().initMediaPlayer(this, uri);
    }

    public void autoNextSong() {
        MusicBuilder.g().getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                MusicBuilder.g().stop();
                MusicBuilder.g().nextSong();
                initMedia();
                sendDataToMP3();
            }
        });
    }

    public void sendDataToMP3() {
        Intent intent = new Intent(ACTION_SEND_SONG_RECEIVER);
        intent.putExtra(SEND_SONG_TO_RECEIVER, songPlaying);
        sendBroadcast(intent);
    }

/*    public void initAction() {
        Log.d("check", "init action");
        // play
        PlayMP3Activity.g().btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
                    MusicBuilder.g().pause();
                    PlayMP3Activity.g().btnPlay.setImageResource(R.drawable.icons_play);
                } else {
                    MusicBuilder.g().play();
                    PlayMP3Activity.g().btnPlay.setImageResource(R.drawable.icons_pause);
                }
            }
        });
        // next
        PlayMP3Activity.g().btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                nextSong();
                initMedia();
            }
        });
        // prev
        PlayMP3Activity.g().btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicBuilder.g().stop();
                preSong();
                initMedia();
            }
        });

        // kéo tua bài hát
        PlayMP3Activity.g().sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicBuilder.g().getMediaPlayer().seekTo(PlayMP3Activity.g().sbTime.getProgress());
            }
        });

        PlayMP3Activity.g().cbRandom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PlayMP3Activity.g().cbRepeat.setChecked(false);
                    if (listSong != null) {
                        listShuffle.addAll(listSong);
                        Collections.shuffle(listShuffle);
                    }
                }
            }
        });

        PlayMP3Activity.g().cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PlayMP3Activity.g().cbRandom.setChecked(false);
                }
            }
        });

        PlayMP3Activity.g().btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayMP3Activity.g().onBackPressed();
            }
        });
    }*/

    private void sendNotification(Song song) {
        Log.d("action", " send");
        Intent intent = new Intent(this, PlayMP3Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, ID_FOREGROUND, intent, PendingIntent.FLAG_UPDATE_CURRENT);

   /*     remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.tvNTitle, song.getTitle());
        remoteViews.setTextViewText(R.id.tvNArtist, song.getArtist());

        Notification notification = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCustomContentView(remoteViews)
                .build();*/

        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_play_media);
        Notification notification = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music_note)
                .addAction(R.drawable.ic_previous, "Previous", null) // #0
                .addAction(R.drawable.ic_pause, "Pause", null)  // #1
                .addAction(R.drawable.ic_next, "Next", null)     // #2
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)//pause
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setLargeIcon(bitmap)
                .build();

        startForeground(ID_FOREGROUND, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
