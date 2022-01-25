package com.example.appmusicmp3;

import android.content.Context;

import androidx.room.Database;
import androidx.room.PrimaryKey;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class}, version = 1)
public abstract class DatabaseSong extends RoomDatabase {
    private static final String DATABASE_NAME = "DATABASE_NAME";
    private static DatabaseSong instance;

    public static synchronized DatabaseSong getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), DatabaseSong.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract DAOSong daoSong();
}
