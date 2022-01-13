package com.example.appmusicmp3;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.AndroidException;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Date;

public class SongService extends Service {

    private ArrayList<Song> listSong = new ArrayList<>();
    private int position;
    public static final int ID_FOREGROUND = 2025;
    public static final int REQUEST_CODE = 3456;


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
        listSong = (ArrayList<Song>) intent.getSerializableExtra(PlayMP3Activity.EXTRA_SERVICE_LIST);
        position = intent.getIntExtra(PlayMP3Activity.EXTRA_SERVICE_POSITION, 0);
        sendNotification(listSong.get(position));
        return START_NOT_STICKY;
    }

    private void sendNotification(Song song) {

        Intent intent = new Intent(this, PlayMP3Activity.class);
        PendingIntent pending = PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        remoteViews.setTextViewText(R.id.tvNTitle, song.getTitle());
        remoteViews.setTextViewText(R.id.tvNArtist, song.getArtist());

        Notification notification = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setContentIntent(pending)
                .setCustomContentView(remoteViews)
                .setLargeIcon()
                .setSound(null)
                .build();
*/
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg_notification);
//        Notification notification = new NotificationCompat.Builder(this, SongApplication.CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_audiotrack)
//                .setContentTitle(song.getTitle())
//                .setContentText(song.getArtist())
//                .setContentIntent(pending)
//                .setLargeIcon(bitmap)
//                .setSound(null)
//                .addAction(R.drawable.ic_previous, "Previous", null) // #0
//                .addAction(R.drawable.ic_pause, "Pause", null)  // #1
//                .addAction(R.drawable.ic_next, "Next", null)     // #2
//                // Apply the media style template
//                .setStyle(new androidx.media.app.Notification.MediaStyle()
//                        .setShowActionsInCompactView(1 /* #1: pause button */)
//                        .setMediaSession(mediaSession.getSessionToken()))
//                .build();
//        startForeground(ID_FOREGROUND, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
