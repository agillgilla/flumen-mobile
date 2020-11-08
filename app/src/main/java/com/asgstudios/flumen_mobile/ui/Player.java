package com.asgstudios.flumen_mobile.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.ImageButton;

import com.asgstudios.flumen_mobile.MainActivity;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.Song;
import com.asgstudios.flumen_mobile.SyncWorker;
import com.asgstudios.flumen_mobile.ui.play.PlayAdapter;

import java.io.File;
import java.io.IOException;

public class Player {

    private MainActivity mainActivity;
    private MediaPlayer mediaPlayer;

    private String playlist;

    private boolean isSongLoaded = false;

    private static Player instance = null;

    private Song currentSong;
    private PlayAdapter.PlayViewHolder currentSongViewHolder;


    public static Player getOrInstantiate(MainActivity mainActivity)
    {
        if (instance == null) {
            instance = new Player(mainActivity);
        }
        return instance;
    }

    public static Player getInstance()
    {
        return instance;
    }

    private Player(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    public void playPauseSong(Song song, PlayAdapter.PlayViewHolder songViewHolder) {
        if (song.equals(currentSong)) {
            this.playPause();
            return;
        }

        if (currentSong != null) {
            ImageButton viewHolderPlayButton = currentSongViewHolder.getPlayButton();
            viewHolderPlayButton.setImageResource(android.R.drawable.ic_media_play);
        }

        File filesDir = mainActivity.getExternalFilesDir(null);
        File musicDir = new File(filesDir, SyncWorker.MUSIC_DIR);

        this.currentSong = song;
        this.currentSongViewHolder = songViewHolder;

        Uri musicUri = Uri.fromFile(new File(new File(musicDir, playlist), song.getFilename()));

        try {
            if (isSongLoaded) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(mainActivity.getApplicationContext(), musicUri);
            mediaPlayer.prepare();
            isSongLoaded = true;
            mediaPlayer.start();

            ImageButton playButton = mainActivity.findViewById(R.id.playButton);
            playButton.setImageResource(android.R.drawable.ic_media_pause);

            ImageButton viewHolderPlayButton = currentSongViewHolder.getPlayButton();
            viewHolderPlayButton.setImageResource(android.R.drawable.ic_media_pause);

        } catch (IOException ioe) {
            System.out.println("IO Exception when playing music: " + ioe.getMessage());
        }
    }

    public void playPause() {
        if (this.isPlaying()) {
            this.pause();
        } else {
            this.play();
        }
    }

    private void pause() {
        this.mediaPlayer.pause();

        ImageButton playButton = mainActivity.findViewById(R.id.playButton);
        playButton.setImageResource(android.R.drawable.ic_media_play);

        ImageButton viewHolderPlayButton = currentSongViewHolder.getPlayButton();
        viewHolderPlayButton.setImageResource(android.R.drawable.ic_media_play);
    }

    private void play() {
        if (isSongLoaded) {
            this.mediaPlayer.start();
        }

        ImageButton playButton = mainActivity.findViewById(R.id.playButton);
        playButton.setImageResource(android.R.drawable.ic_media_pause);

        ImageButton viewHolderPlayButton = currentSongViewHolder.getPlayButton();
        viewHolderPlayButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    public boolean isPlaying() {
        return this.mediaPlayer.isPlaying();
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }
}
