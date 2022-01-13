package com.example.appmusicmp3;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class SongApplication extends Application {

    public static final String CHANNEL_ID = "CHANNEL_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        addNotificationChanel();
    }

    private void addNotificationChanel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Channel MP3", NotificationManager.IMPORTANCE_LOW);
            NotificationManager mManager = getSystemService(NotificationManager.class);
            if (mManager != null) {
                mManager.createNotificationChannel(mChannel);
            }
        }
    }
}
