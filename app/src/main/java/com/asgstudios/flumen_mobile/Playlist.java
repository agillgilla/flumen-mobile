package com.asgstudios.flumen_mobile;

public class Playlist {
    private String playlistName;
    private int playlistIndex;

    public Playlist(String playlistName, int playlistIndex) {
        this.playlistName = playlistName;
        this.playlistIndex = playlistIndex;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public int getPlaylistIndex() {
        return playlistIndex;
    }

    @Override
    public String toString() {
        return this.playlistName;
    }
}
