package com.example.appmusicmp3;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Comparator;

@Entity(tableName = "ListSong")
public class Song implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int key;
    private String id;
    private String title;
    private String artist;
    private String data;

    public Song(String id, String title, String artist, String data) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.data = data;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
