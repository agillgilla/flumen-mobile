package com.asgstudios.flumen_mobile.ui.play;

import android.app.Application;
import android.app.Notification;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.media.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.asgstudios.flumen_mobile.PlayQueue;
import com.asgstudios.flumen_mobile.PlayShuffleQueue;
import com.asgstudios.flumen_mobile.PlaybackInfo;
import com.asgstudios.flumen_mobile.Playlist;
import com.asgstudios.flumen_mobile.PlaylistManager;
import com.asgstudios.flumen_mobile.Preferences;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.Song;
import com.asgstudios.flumen_mobile.Player;
import com.asgstudios.flumen_mobile.SongAndIndex;

import java.util.List;

public class PlayViewModel extends AndroidViewModel {

    public enum PlayMode { LOOP, SHUFFLE }

    private Player player;

    private PlayShuffleQueue playShuffleQueue;

    private PlayQueue playQueue;

    private PlaylistManager playlistManager;

    private PlayAdapter playAdapter;

    private MutableLiveData<Integer> playingIndex;

    private MutableLiveData<Boolean> isPlaying;

    private MutableLiveData<PlaybackInfo> currSongPlaybackInfo;
    private MutableLiveData<String> currSongTime;
    private MutableLiveData<String> currSongDuration;

    private MutableLiveData<Song> currSong;
    private MutableLiveData<Playlist> currPlaylist;

    private MutableLiveData<PlayMode> playMode;

    private MutableLiveData<List<Playlist>> playlists;

    private MutableLiveData<List<Song>> songs;

    public PlayViewModel(Application application) {
        super(application);

        this.player = Player.getOrInstantiate(getApplication());
        this.player.setViewModel(this);

        this.playlistManager = new PlaylistManager(getApplication().getExternalFilesDir(null));

        this.playlists = new MutableLiveData<>();
        this.playlists.setValue(playlistManager.getPlaylists());

        Preferences preferences = Preferences.getInstance();
        int lastPlaylistIndex = preferences.getLastPlaylistIndex();

        this.songs = new MutableLiveData<>();
        if (this.playlists.getValue().size() > 0) {
            this.songs.setValue(playlistManager.getPlaylistSongs(this.playlists.getValue().get(lastPlaylistIndex), true));
        }

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
        this.currSong.setValue(new Song("", "", "", 0, ""));

        this.currPlaylist = new MutableLiveData<>();
        if (playlistManager.loadPlaylistList().size() > 0) {
            this.currPlaylist.setValue(playlistManager.loadPlaylistList().get(lastPlaylistIndex));
        }

        this.playMode = new MutableLiveData<>();
        this.playMode.setValue(PlayMode.SHUFFLE);

        this.playShuffleQueue = PlayShuffleQueue.getOrInstantiate(playlistManager);

        this.playQueue = PlayQueue.getOrInstantiate();
    }

    public void playIfNotAlready() {
        if (!this.isPlaying.getValue()) {
            this.playPause();
        }
    }

    public void pauseIfNotAlready() {
        if (this.player.isSongLoaded() && this.isPlaying.getValue()) {
            this.playPause();
        }
    }

    public boolean playPause() {
        if (!this.player.isSongLoaded()) {
            this.nextSong();

            return true;
        }

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

    public boolean playPauseSong(Song song) {
        if (song.equals(currSong.getValue())) {
            return this.playPause();
        }

        playNewSong(song);
        return true;
    }

    public void playNewSong(Song song) {
        if (song == null) {
            return;
        }

        this.player.setPlaylist(currPlaylist.getValue());
        boolean isPlaying = this.player.playNewSong(song);
        this.isPlaying.setValue(isPlaying);
    }

    public void beginSeek() {
        player.beginSeek();
    }

    public void finalizeSeek(int progress) {
        player.finalizeSeek(progress);
    }

    public void nextSong() {
        if (this.getPlayMode().getValue() == PlayMode.LOOP) {
            this.player.restartSong();
        } else if (this.getPlayMode().getValue() == PlayMode.SHUFFLE) {
            if (!playQueue.isEmpty()) {
                SongAndIndex nextSongAndIndex = playQueue.pop();

                playAdapter.playingIndex = nextSongAndIndex.getIndex();
                this.setPlayingIndex(playAdapter.playingIndex);
                playAdapter.notifyDataSetChanged();

                this.playNewSong(nextSongAndIndex.getSong());
            } else {
                playAdapter.playingIndex = playShuffleQueue.peekNextSongIndex();

                this.setPlayingIndex(playAdapter.playingIndex);
                playAdapter.notifyDataSetChanged();

                if (playAdapter.playingIndex > -1) {
                    this.playNewSong(playShuffleQueue.nextSong());
                }
            }
        }
    }

    public void previousSong() {
        if (player.getCurrTimeMillis() > 5000) {
            player.restartSong();
            return;
        }

        playAdapter.playingIndex = playShuffleQueue.peekPreviousSongIndex();
        this.setPlayingIndex(playAdapter.playingIndex);
        playAdapter.notifyDataSetChanged();

        this.playPauseSong(playShuffleQueue.previousSong());
    }

    public void pushSongIndexToQueue(int songIndex) {
        playQueue.push(songs.getValue().get(songIndex), songIndex);
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
        this.songs.setValue(playlistManager.getPlaylistSongs(currPlaylist, true));
        this.currPlaylist.setValue(currPlaylist);
        this.playShuffleQueue.setActivePlaylist(currPlaylist);

        if (player.getPlaylist() != null &&
                currPlaylist.getPlaylistName().equals(player.getPlaylist().getPlaylistName())) {
            playAdapter.playingIndex = this.playingIndex.getValue();
            playAdapter.notifyDataSetChanged();
        }
        this.player.setPlaylist(currPlaylist);
    }

    public void setPlaylistIndex(int playlistIndex) {
        setCurrPlaylist(playlists.getValue().get(playlistIndex));
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

    public void setPlayMode(PlayMode playMode) {
        this.playMode.setValue(playMode);
    }

    public MutableLiveData<PlayMode> getPlayMode() {
        return playMode;
    }

    public void setPlayAdapter(PlayAdapter playAdapter) {
        this.playAdapter = playAdapter;
    }

    public PlayAdapter getPlayAdapter() {
        return this.playAdapter;
    }
}