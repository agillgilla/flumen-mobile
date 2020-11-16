package com.asgstudios.flumen_mobile;

import java.util.Objects;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Playlist) {
            Playlist otherPlaylist = (Playlist) other;

            return this.playlistName.equals(otherPlaylist.getPlaylistName()) &&
                    this.playlistIndex == otherPlaylist.getPlaylistIndex();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistName, playlistIndex);
    }
}
