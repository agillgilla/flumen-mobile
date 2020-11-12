package com.asgstudios.flumen_mobile;


public class Song {

    private String name;
    private String artist;
    private int length;
    private String filename;

    public Song(String name, String artist, int length, String filename) {
        this.name = name;
        this.artist = artist;
        this.length = length;
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
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
