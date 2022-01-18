package com.example.appmusicmp3;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicBuilder {
    private static int PAUSE = 0;
    private static int PLAYING = 1;
    private static int STOP = 2;
    private static MusicBuilder instance;
    private static MediaPlayer mediaPlayer;
    private int status = 0;
    private ArrayList<Song> listSong = new ArrayList<>();
    private ArrayList<Song> listShuffle = new ArrayList<>();
    private Song songPlaying;
    private boolean isRandom;
    private boolean isRepeat;

    public static MusicBuilder g() {
        if (instance == null) {
            instance = new MusicBuilder();
        }
        return instance;
    }

    public void setListSong(List<Song> list) {
        listSong.clear();
        listSong.addAll(list);
    }

    public void setListShuffle() {
        if (listSong != null) {
            listShuffle.addAll(listSong);
            Collections.shuffle(listShuffle);
        }
    }

    public void initMediaPlayer(Context context, Uri uri) {
        stop();
        mediaPlayer = MediaPlayer.create(context, uri);
        status = STOP;
    }

    public Song getSongPlaying() {
        return songPlaying;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public void setRandom(boolean random) {
        isRandom = random;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public void play() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.start();
            status = PLAYING;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.pause();
            status = PAUSE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            status = STOP;
        } catch (Exception e) {

        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void nextSong() {
        Log.d("check", "next song");
        if (isRandom) {
            if (listShuffle != null && listShuffle.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listShuffle);
            if (position >= 0) {
                if (position == listShuffle.size() - 1) {
                    songPlaying = listShuffle.get(0);
                } else {
                    songPlaying = listShuffle.get(position + 1);
                }
            }

        } else if (isRepeat) {
            if (listSong != null && listSong.isEmpty()) {
                return;
            } else {
                int position = getIndexOfFirst(songPlaying, listSong);
                songPlaying = listSong.get(position);
            }


        } else {
            if (listSong != null && listSong.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listSong);
            if (position >= 0) {
                if (position == listSong.size() - 1) {
                    songPlaying = listSong.get(0);
                } else {
                    songPlaying = listSong.get(position + 1);
                }
            }
        }
    }

    public void preSong() {
        Log.d("check", "prev song");
        if (isRandom) {
            if (listShuffle != null && listShuffle.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listShuffle);
            if (position > 0) {
                songPlaying = listShuffle.get(position - 1);
            }
            if (position == 0) {
                songPlaying = listShuffle.get(0);
            }

        } else if (isRepeat) {
            if (listSong != null && listSong.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listSong);
            songPlaying = listSong.get(position);

        } else {
            if (listSong != null && listSong.isEmpty()) {
                return;
            }
            int position = getIndexOfFirst(songPlaying, listSong);
            if (position > 0) {
                songPlaying = listSong.get(position - 1);
            }
        }
    }

    public int getIndexOfFirst(Song songSelected, List<Song> list) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        int indexMatch = -1;
        for (int i = 0; i < list.size(); i++) {
            if (songSelected.getId().equals(list.get(i).getId())) {
                indexMatch = i;
            }
        }
        Log.d("check", indexMatch+"");
        return indexMatch;
    }
}
