package com.example.appmusicmp3;

import android.os.Parcelable;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private String artist;
    private String data;

    public Song (String title, String artist, String data) {
        this.title = title;
        this.artist = artist;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
