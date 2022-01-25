package com.example.appmusicmp3;

import java.util.Comparator;

public class CompareToTiTle implements Comparator<Song> {
    @Override
    public int compare(Song o1, Song o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        int value = o1.getTitle().compareToIgnoreCase(o2.getTitle());
        return value;
    }
}
