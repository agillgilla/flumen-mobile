package com.asgstudios.flumen_mobile.ui.play;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.asgstudios.flumen_mobile.PlaybackInfo;
import com.asgstudios.flumen_mobile.Playlist;
import com.asgstudios.flumen_mobile.PlaylistManager;
import com.asgstudios.flumen_mobile.Song;
import com.asgstudios.flumen_mobile.Player;

import java.util.List;

public class PlayViewModel extends AndroidViewModel {

    private Player player;

    private PlaylistManager playlistManager;

    private PlayAdapter playAdapter;

    private MutableLiveData<Integer> playingIndex;

    private MutableLiveData<Boolean> isPlaying;

    private MutableLiveData<PlaybackInfo> currSongPlaybackInfo;
    private MutableLiveData<String> currSongTime;
    private MutableLiveData<String> currSongDuration;

    private MutableLiveData<Song> currSong;

    private MutableLiveData<Playlist> currPlaylist;

    private MutableLiveData<List<Playlist>> playlists;

    private MutableLiveData<List<Song>> songs;

    public PlayViewModel(Application application) {
        super(application);

        System.out.println("PlayViewModel created!");

        this.player = Player.getOrInstantiate(getApplication());
        this.player.setViewModel(this);

        this.playlistManager = new PlaylistManager(getApplication().getExternalFilesDir(null));

        this.playlists = new MutableLiveData<>();
        this.playlists.setValue(playlistManager.loadPlaylistList());

        this.songs = new MutableLiveData<>();
        this.songs.setValue(playlistManager.getPlaylistSongs(this.playlists.getValue().get(0)));

        this.playingIndex = new MutableLiveData<>();
        this.playingIndex.setValue(-1);

        this.isPlaying = new MutableLiveData<>();
        this.isPlaying.setValue(false);

        this.currSongPlaybackInfo = new MutableLiveData<>();
        this.currSongPlaybackInfo.setValue(new PlaybackInfo(0));

        this.currSongTime = new MutableLiveData<>();
        this.currSongTime.setValue(secondsToFormatted(0));

        this.currSongDuration = new MutableLiveData<>();
        this.currSongDuration.setValue(secondsToFormatted(0));

        this.currSong = new MutableLiveData<>();
        this.currSong.setValue(new Song("", "", 0, ""));

        this.currPlaylist = new MutableLiveData<>();
        this.currPlaylist.setValue(playlistManager.loadPlaylistList().get(0));
    }

    public boolean playPause() {
        boolean isPlaying = this.player.playPause();

        this.isPlaying.setValue(isPlaying);

        if (isPlaying) {
            if (playAdapter.playingIndex == -1 && playAdapter.pausedIndex != -1) {
                playAdapter.playingIndex = playAdapter.pausedIndex;
                this.playingIndex.setValue(playAdapter.playingIndex);
                playAdapter.pausedIndex = -1;
                playAdapter.notifyItemChanged(playAdapter.playingIndex);
            }
        } else {
            playAdapter.pausedIndex = playAdapter.playingIndex;
            playAdapter.playingIndex = -1;
            this.playingIndex.setValue(playAdapter.playingIndex);
            playAdapter.notifyItemChanged(playAdapter.pausedIndex);
        }

        return isPlaying;
    }

    public boolean playPauseSong(Song song, PlayAdapter.PlayViewHolder holder) {
        if (song.equals(currSong.getValue())) {
            return this.playPause();
        }

        this.player.setPlaylist(currPlaylist.getValue());
        boolean isPlaying = this.player.playNewSong(song, holder);
        this.isPlaying.setValue(isPlaying);
        return isPlaying;
    }

    public void beginSeek() {
        player.beginSeek();
    }

    public void finalizeSeek(int progress) {
        player.finalizeSeek(progress);
    }

    public static String secondsToFormatted(int seconds) {
        int mins = seconds / 60;
        seconds = seconds % 60;

        return String.format("%d:%02d", mins, seconds);
    }

    public MutableLiveData<String> getCurrSongTime() {
        return currSongTime;
    }

    public void setCurrSongTime(long currSongTime) {
        this.currSongTime.setValue(secondsToFormatted((int) (currSongTime / 1000.0f)));
        PlaybackInfo playbackInfo = this.currSongPlaybackInfo.getValue();
        playbackInfo.setCurrSongTime(currSongTime);
        this.currSongPlaybackInfo.setValue(playbackInfo);
    }

    public MutableLiveData<String> getCurrSongDuration() {
        return currSongDuration;
    }

    public void setCurrSongDuration(long currSongDuration) {
        this.currSongDuration.setValue(secondsToFormatted((int) (currSongDuration / 1000.0f)));
        PlaybackInfo playbackInfo = this.currSongPlaybackInfo.getValue();
        playbackInfo.setCurrSongDuration(currSongDuration);
        this.currSongPlaybackInfo.setValue(playbackInfo);
    }

    public MutableLiveData<Song> getCurrSong() {
        return currSong;
    }

    public void setCurrSong(Song currSong) {
        this.currSong.setValue(currSong);
    }

    public MutableLiveData<Playlist> getCurrPlaylist() {
        return currPlaylist;
    }

    public void setCurrPlaylist(Playlist currPlaylist) {
        this.songs.setValue(playlistManager.getPlaylistSongs(currPlaylist));
        this.currPlaylist.setValue(currPlaylist);

        if (player.getPlaylist() != null &&
                currPlaylist.getPlaylistName().equals(player.getPlaylist().getPlaylistName())) {
            playAdapter.playingIndex = this.playingIndex.getValue();
            playAdapter.notifyDataSetChanged();
        }
    }

    public void setPlaylistIndex(int playlistIndex) {
        this.currPlaylist.setValue(playlists.getValue().get(playlistIndex));
        this.songs.setValue(playlistManager.getPlaylistSongs(this.playlists.getValue().get(playlistIndex)));
    }

    public Playlist getPlayingPlaylist() {
        return this.player.getPlaylist();
    }

    public MutableLiveData<List<Playlist>> getPlaylists() {
        return playlists;
    }

    public MutableLiveData<List<Song>> getSongs() {
        return songs;
    }

    public void setPlayingIndex(int playingIndex) {
        this.playingIndex.setValue(playingIndex);
    }

    public MutableLiveData<Integer> getPlayingIndex() {
        return this.playingIndex;
    }

    public MutableLiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying.setValue(isPlaying);
    }

    public MutableLiveData<PlaybackInfo> getCurrSongPlaybackInfo() {
        return currSongPlaybackInfo;
    }

    public void setPlayAdapter(PlayAdapter playAdapter) {
        this.playAdapter = playAdapter;
    }

    public PlayAdapter getPlayAdapter() {
        return this.playAdapter;
    }
}