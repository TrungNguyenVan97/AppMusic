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
import android.provider.SyncStateContract;
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
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.nio.file.ProviderNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongService extends Service {

    private Song songPlaying;
    RemoteViews notificationLayout;
    private SongBinder mBinder = new SongBinder();

    private BroadcastReceiver broadcastNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_MP3_NEXT.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
            }
        }
    };

    private BroadcastReceiver broadcastPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_MP3_PREV.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
            }
        }
    };

    private BroadcastReceiver broadcastPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.ACTION_MP3_PLAY_PAUSE.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
            }
        }
    };

    private BroadcastReceiver layoutBottomPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.LAYOUT_BOTTOM_PLAY_PAUSE.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
            }
        }
    };

    private BroadcastReceiver notificationPlayPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_PLAY_PAUSE.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
            }
        }
    };

    private BroadcastReceiver notificationNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_NEXT.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
            }
        }
    };

    private BroadcastReceiver notificationPrev = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Tags.NOTIFICATION_PREV.equals(intent.getAction())) {
                sendNotification(MusicBuilder.g().getSongPlaying());
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
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");
        registerReceiver();
        songPlaying = (Song) intent.getSerializableExtra(Tags.EXTRA_SERVICE_SONG);
        initMedia();
        sendNotification(songPlaying);
        autoNextSong();
        return START_NOT_STICKY;
    }

    private void initMedia() {
        Log.d("service", "initMedia");
        if (songPlaying == null) {
            return;
        }
        MusicBuilder.g().initMediaPlayer(this, songPlaying);
        MusicBuilder.g().play();
    }

    public void autoNextSong() {
        MusicBuilder.g().setCallBack(new MusicBuilder.CallBack() {
            @Override
            public void onSongCompletion() {
                MusicBuilder.g().nextSong();
                songPlaying = MusicBuilder.g().getSongPlaying();
                initMedia();
                sendNotification(songPlaying);

                Intent intentPlay = new Intent(Tags.ACTION_SERVICE_AUTO_NEXT);
                sendBroadcast(intentPlay);
            }
        });
    }

    public void sendNotification(Song song) {
        Log.d("service", " sendNotification");
        if (MusicBuilder.g().getSongPlaying() == null) {
            return;
        }
        Intent intent = new Intent(this, PlayMP3Activity.class);
        intent.putExtra(Tags.IS_FROM_SERVICE, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.tvNTitle, song.getTitle());
        notificationLayout.setTextViewText(R.id.tvNArtist, song.getArtist());
        if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
            notificationLayout.setImageViewResource(R.id.btnNPlay, R.drawable.ic_pause);
        } else {
            notificationLayout.setImageViewResource(R.id.btnNPlay, R.drawable.ic_play);
        }
        // play-pause
        Intent intentPlay = new Intent(this, MyReceiver.class).setAction(Tags.ACTION_PLAY_PAUSE);
        PendingIntent pIntentPlayPause = PendingIntent.getBroadcast(this, Tags.PENDING_PLAY_PAUSE, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.btnNPlay, pIntentPlayPause);
        //next
        Intent intentNext = new Intent(this, MyReceiver.class).setAction(Tags.ACTION_NEXT);
        PendingIntent pIntentNext = PendingIntent.getBroadcast(this, Tags.PENDING_NEXT, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.btnNNext, pIntentNext);
        //prev
        Intent intentPrev = new Intent(this, MyReceiver.class).setAction(Tags.ACTION_PREV);
        PendingIntent pIntentPrev = PendingIntent.getBroadcast(this, Tags.PENDING_PREV, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.btnNPrev, pIntentPrev);

        Notification notification = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.icons_music_note)
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout)
                .build();

        startForeground(Tags.ID_FOREGROUND, notification);
    }

    private void registerReceiver() {
        IntentFilter filterNext = new IntentFilter(Tags.ACTION_MP3_NEXT);
        registerReceiver(broadcastNext, filterNext);

        IntentFilter filterPrev = new IntentFilter(Tags.ACTION_MP3_PREV);
        registerReceiver(broadcastPrev, filterPrev);

        IntentFilter filterPlayPause = new IntentFilter(Tags.ACTION_MP3_PLAY_PAUSE);
        registerReceiver(broadcastPlayPause, filterPlayPause);

        IntentFilter filterLayoutBottomPlayPause = new IntentFilter(Tags.LAYOUT_BOTTOM_PLAY_PAUSE);
        registerReceiver(layoutBottomPlayPause, filterLayoutBottomPlayPause);

        IntentFilter filterNotificationPlayPause = new IntentFilter(Tags.NOTIFICATION_PLAY_PAUSE);
        registerReceiver(notificationPlayPause, filterNotificationPlayPause);

        IntentFilter filterNotificationNext = new IntentFilter(Tags.NOTIFICATION_NEXT);
        registerReceiver(notificationNext, filterNotificationNext);

        IntentFilter filterNotificationPrev = new IntentFilter(Tags.NOTIFICATION_PREV);
        registerReceiver(notificationPrev, filterNotificationPrev);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastNext);
        unregisterReceiver(broadcastPrev);
        unregisterReceiver(broadcastPlayPause);
        unregisterReceiver(layoutBottomPlayPause);
        unregisterReceiver(notificationPlayPause);
        unregisterReceiver(notificationNext);
        unregisterReceiver(notificationPrev);
    }
}
