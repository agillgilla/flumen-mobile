package com.asgstudios.flumen_mobile.ui;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.ImageButton;

import com.asgstudios.flumen_mobile.MainActivity;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.SyncWorker;

import java.io.File;
import java.io.IOException;

public class Player {

    private MainActivity mainActivity;
    private MediaPlayer mediaPlayer;

    private String playlist;

    public Player(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    public void playSong(String songFile) {
        File filesDir = mainActivity.getExternalFilesDir(null);
        File musicDir = new File(filesDir, SyncWorker.MUSIC_DIR);

        Uri musicUri = Uri.fromFile(new File(new File(musicDir, playlist), songFile));

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(mainActivity.getApplicationContext(), musicUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            ImageButton playButton = mainActivity.findViewById(R.id.playButton);
            playButton.setImageResource(android.R.drawable.ic_media_pause);
        } catch (IOException ioe) {
            System.out.println("IO Exception when playing music: " + ioe.getMessage());
        }
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }
}
