package com.example.appmusicmp3;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.nio.file.ProviderNotFoundException;
import java.util.ArrayList;

public class SongService extends Service {

    public static final String ACTION_MUSIC = "ACTION_MUSIC";
    private ArrayList<Song> listSong = new ArrayList<>();
    private Song songPlaying;
    private static final int ID_FOREGROUND = 2025;

    private static final int ACTION_PREVIOUS = 1234;
    private static final int ACTION_PAUSE = 1235;
    private static final int ACTION_NEXT = 1236;
    private static final int ACTION_CLEAR = 1237;
    private RemoteViews remoteViews;

    public static final String MY_ACTION = "MY_ACTION";

    //private SongBinder mBinder = new SongBinder();

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
        Log.d("action", "onStartCommand");
        listSong = (ArrayList<Song>) intent.getSerializableExtra(PlayMP3Activity.EXTRA_SERVICE_LIST);
        songPlaying = (Song) intent.getSerializableExtra(PlayMP3Activity.EXTRA_SERVICE_POSITION);
        sendNotification(songPlaying);

        return START_NOT_STICKY;
    }

  /*  public class SongBinder extends Binder {
        SongService getSongService() {
            return SongService.this;
        }
    }*/

    /*private void initAction(int action) {
        Log.d("action", " action");
        switch (action) {
            case ACTION_PREVIOUS:
                break;

            case ACTION_PAUSE:
                actionPause();
                sendNotification(songPlaying);
                break;

            case ACTION_NEXT:
                break;

            case ACTION_CLEAR:
                stopSelf();
                break;
        }
    }*/

   /* private void actionPause() {
        Log.d("action", " pause");
        if (MusicBuilder.g().getMediaPlayer().isPlaying()) {
            MusicBuilder.g().pause();
            remoteViews.setImageViewResource(R.id.btnNPlay, R.drawable.ic_play);
        } else {
            MusicBuilder.g().play();
            remoteViews.setImageViewResource(R.id.btnNPlay, R.drawable.ic_pause);
        }
    }*/

    private void sendNotification(Song song) {
        Log.d("action", " send");
        Intent intent = new Intent(this, PlayMP3Activity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(SongService.this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(ID_FOREGROUND, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.tvNTitle, song.getTitle());
        remoteViews.setTextViewText(R.id.tvNArtist, song.getArtist());

       /* remoteViews.setOnClickPendingIntent(R.id.btnNPlay, getPenDingIntent(this, ACTION_PAUSE));

        remoteViews.setOnClickPendingIntent(R.id.btnNClear, getPenDingIntent(this, ACTION_CLEAR));*/

        Notification notification = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .build();

        startForeground(ID_FOREGROUND, notification);
    }

  /*  private PendingIntent getPenDingIntent(Context context, int action) {
        Log.d("action", " Pending");
        Intent intent = new Intent(MY_ACTION);
        intent.putExtra(ACTION_MUSIC, action);
        sendBroadcast(intent);
        return PendingIntent.getBroadcast(getApplicationContext(), action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicBuilder.g().stop();
    }
}
