package com.example.appmusicmp3;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface DAOSong {
    @Insert
    void insertSong(Song song);

    @Query("SELECT * FROM ListSong")
    List<Song> getListSong();

    @Query("SELECT * FROM ListSong where id = :id")
    List<Song> checkSong(String id);

    @Delete
    void deleteSong(Song song);

    @Query("DELETE  FROM ListSong")
    void deleteListSong();
}
