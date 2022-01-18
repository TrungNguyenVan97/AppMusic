package com.example.appmusicmp3;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class AsyncTaskGetList extends AsyncTask<Void, Void, List<Song>> {

    SongAdapter adapter;
    Activity activity;

    public AsyncTaskGetList(SongAdapter adapter, Activity activity) {
        this.adapter = adapter;
        this.activity = activity;
    }

    @Override
    protected List<Song> doInBackground(Void... voids) {
        return getListSong();
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        super.onPostExecute(songs);
        if (adapter != null) {
        }
    }

    private List<Song> getListSong() {

        ArrayList<Song> listSong = new ArrayList<>();
        ContentResolver contentResolver = activity.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Audio.Media.TITLE_KEY);

        if (cursor != null && cursor.moveToFirst()) {
            int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int indexArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int indexID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

            do {
                String currentTitle = cursor.getString(indexTitle);
                String currentArtist = cursor.getString(indexArtist);
                String currentData = cursor.getString(indexData);
                String currentID = cursor.getString(indexID);
                listSong.add(new Song(currentID, currentTitle, currentArtist, currentData));
            } while (cursor.moveToNext());
        }
        MusicBuilder.g().setListSong(listSong);
        return listSong;
    }
}
