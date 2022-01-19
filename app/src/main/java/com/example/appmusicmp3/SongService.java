package com.example.appmusicmp3;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.annotation.NonNull;
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
    public static final String SEND_TO_PLAY = "SEND_TO_PLAY";
    public static final String ACTION_SEND_PlAY = "ACTION_SEND_PlAY";
    public static final String SEND_TO_MAIN = "SEND_TO_MAIN";
    public static final String ACTION_SEND_MAIN = "ACTION_SEND_MAIN";
    private Song songPlaying;
    private SongBinder mBinder = new SongBinder();

    private BroadcastReceiver broadcastNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PlayMP3Activity.ACTION_NEXT.equals(intent.getAction())) {
                songPlaying = (Song) intent.getSerializableExtra(PlayMP3Activity.EXTRA_NEXT);
                sendNotification(songPlaying);
            }
        }
    };

    private BroadcastReceiver broadcastPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PlayMP3Activity.ACTION_PREV.equals(intent.getAction())) {
                songPlaying = (Song) intent.getSerializableExtra(PlayMP3Activity.EXTRA_PREV);
                sendNotification(songPlaying);
            }
        }
    };

    public class SongBinder extends Binder {
        SongService getSongService() {
            return SongService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");
        IntentFilter filterNext = new IntentFilter(PlayMP3Activity.ACTION_NEXT);
        registerReceiver(broadcastNext, filterNext);

        IntentFilter filterPrev = new IntentFilter(PlayMP3Activity.ACTION_PREV);
        registerReceiver(broadcastPrev, filterPrev);

        songPlaying = (Song) intent.getSerializableExtra(MainActivity.EXTRA_SERVICE_SONG);
        initMedia();
        sendNotification(songPlaying);
        autoNextSong();

        return START_NOT_STICKY;
    }

    private void initMedia() {
        Log.d("service", "initMedia");
        setData();
        MusicBuilder.g().play();
    }

    private void setData() {
        Log.d("service", "setData");
        if (songPlaying == null) {
            return;
        }
        MusicBuilder.g().initMediaPlayer(this, songPlaying);
    }

    public void autoNextSong() {
        MusicBuilder.g().setCallBack(new MusicBuilder.CallBack() {
            @Override
            public void onSongCompletion() {
                MusicBuilder.g().nextSong();
                songPlaying = MusicBuilder.g().getSongPlaying();
                initMedia();
                sendNotification(songPlaying);
                sendData();
                Log.d("service", "autoNext");
            }
        });
    }

    private void sendData() {
        Log.d("service", "senData");
        Intent intentPlay = new Intent(ACTION_SEND_PlAY);
        intentPlay.putExtra(SEND_TO_PLAY, songPlaying);
        sendBroadcast(intentPlay);

        Intent intentMain = new Intent(ACTION_SEND_MAIN);
        intentMain.putExtra(SEND_TO_MAIN, songPlaying);
        sendBroadcast(intentMain);
    }

    public void sendNotification(Song song) {
        Log.d("action", " send");
        /*Intent intent = new Intent(this, PlayMP3Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, ID_FOREGROUND, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/

        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_play_media);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music_note)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .addAction(R.drawable.ic_previous, "Previous", null)
                .addAction(R.drawable.ic_pause, "Pause", null)
                .addAction(R.drawable.ic_next, "Next", null)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist())
                .setLargeIcon(bitmap);
        Notification notification = builder.build();
        startForeground(ID_FOREGROUND, notification);
    }

    public Song getSongPlaying() {
        return songPlaying;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastNext);
        unregisterReceiver(broadcastPrev);
    }
}
