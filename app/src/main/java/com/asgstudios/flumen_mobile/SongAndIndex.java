package com.asgstudios.flumen_mobile;

public class SongAndIndex {
    private Song song;
    private int index;

    public SongAndIndex(Song song, int index) {
        this.song = song;
        this.index = index;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
