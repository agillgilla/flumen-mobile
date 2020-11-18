package com.asgstudios.flumen_mobile;

import android.app.Application;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import com.asgstudios.flumen_mobile.ui.play.PlayViewModel;

import java.io.File;
import java.io.IOException;

public class Player {

    private static Player instance = null;

    private Application application;
    private PlayViewModel viewModel;
    private Preferences preferences;

    private MediaPlayer mediaPlayer;

    private Playlist playlist;

    private boolean isSongLoaded = false;

    private Song currentSong;

    private Handler updateTimeHandler;
    private Runnable updateTimeTask;


    public static Player getOrInstantiate(Application application)
    {
        if (instance == null) {
            instance = new Player(application);
        }
        return instance;
    }

    public static Player getInstance()
    {
        return instance;
    }

    private Player(Application application) {
        this.application = application;

        this.preferences = Preferences.getInstance();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                viewModel.nextSong();
            }
        });
    }

    public boolean playNewSong(Song song) {

        File filesDir = application.getExternalFilesDir(null);
        File musicDir = new File(filesDir, SyncWorker.MUSIC_DIR);

        this.currentSong = song;

        Uri musicUri = Uri.fromFile(new File(new File(musicDir, playlist.getPlaylistName()), song.getFilename()));

        try {
            if (isSongLoaded) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(application.getApplicationContext(), musicUri);
            mediaPlayer.prepare();
            isSongLoaded = true;
            mediaPlayer.start();

            updateTimeHandler = new Handler();

            updateTimeTask = new Runnable() {
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        long totalDuration = mediaPlayer.getDuration();
                        long currentDuration = mediaPlayer.getCurrentPosition();

                        viewModel.setCurrSongTime(currentDuration);
                        viewModel.setCurrSongDuration(totalDuration);
                    }

                    // Running this thread after 100 milliseconds
                    updateTimeHandler.postDelayed(this, 100);
                }
            };
            updateTimeHandler.postDelayed(updateTimeTask, 100);

            viewModel.setCurrSong(song);

            return true;
        } catch (IOException ioe) {
            System.out.println("IO Exception when playing music: " + ioe.getMessage());
        }
        return false;
    }

    /**
     * Play or pause the current song.
     * @return True if the song is playing after this method call, and false otherwise.
     */
    public boolean playPause() {
        if (this.isPlaying()) {
            this.pause();
        } else {
            this.play();
        }

        viewModel.setIsPlaying(this.isPlaying());

        return this.isPlaying();
    }

    private void pause() {
        if (this.isPlaying()) {
            this.mediaPlayer.pause();

            //ImageButton playButton = mainActivity.findViewById(R.id.playButton);
            //playButton.setImageResource(android.R.drawable.ic_media_play);

            //ImageButton viewHolderPlayButton = currentSongViewHolder.getPlayButton();
            //viewHolderPlayButton.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void play() {
        if (isSongLoaded) {
            this.mediaPlayer.start();

            //ImageButton playButton = mainActivity.findViewById(R.id.playButton);
            //playButton.setImageResource(android.R.drawable.ic_media_pause);

            //ImageButton viewHolderPlayButton = currentSongViewHolder.getPlayButton();
            //viewHolderPlayButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    public void restartSong() {
        this.mediaPlayer.seekTo(0);
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void beginSeek() {
        updateTimeHandler.removeCallbacks(updateTimeTask);
    }

    public void finalizeSeek(int progress) {
        //int totalDuration = mp.getDuration();
        //int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        System.out.println("Seeking to: " + progress * 10);
        mediaPlayer.seekTo(progress * 100);

        // update timer progress again
        updateTimeHandler.postDelayed(updateTimeTask, 100);
    }

    public boolean isPlaying() {
        return this.mediaPlayer.isPlaying();
    }

    public boolean isSongLoaded() {
        return this.isSongLoaded;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.preferences.setLastPlaylistIndex(playlist.getPlaylistIndex());
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setViewModel(PlayViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public PlayViewModel getViewModel() {
        return this.viewModel;
    }

    public int getCurrTimeMillis() {
        return this.mediaPlayer.getCurrentPosition();
    }
}
