package com.asgstudios.flumen_mobile;

public class PlaybackInfo {

    private long currSongTime;
    private long currSongDuration;

    public PlaybackInfo(long currSongTime, long currSongDuration) {
        this.currSongTime = currSongTime;
        this.currSongDuration = currSongDuration;
    }

    public PlaybackInfo(int currSongDuration) {
        this(0, currSongDuration);
    }

    public long getCurrSongTime() {
        return currSongTime;
    }

    public void setCurrSongTime(long currSongTime) {
        this.currSongTime = currSongTime;
    }

    public long getCurrSongDuration() {
        return currSongDuration;
    }

    public void setCurrSongDuration(long currSongDuration) {
        this.currSongDuration = currSongDuration;
    }
}
