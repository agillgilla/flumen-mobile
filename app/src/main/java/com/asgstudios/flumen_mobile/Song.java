package com.asgstudios.flumen_mobile;


public class Song {

    private String name;
    private String artist;
    private String album;
    private int length;
    private String filename;

    public Song(String name, String artist, String album, int length, String filename) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.length = length;
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getLength() {
        return length;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Song) {
            Song otherSong = (Song) other;

            return this.filename.equals(otherSong.getFilename()) && this.length == otherSong.getLength();
        }

        return false;
    }
}
