package com.asgstudios.flumen_mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.app.NotificationCompat;

import com.asgstudios.flumen_mobile.ui.play.PlayViewModel;

import java.util.List;


public class MusicService extends MediaBrowserServiceCompat {

    private static final String MEDIA_SESSION_TAG = "FLUMEN";
    private static final int NOTIFICATION_ID = 412;
    public static final String NOTIFICATION_CHANNEL = "com.agillitystudios.flumen_mobile.ANDROID";

    public final static String UPDATE_SONG_ACTION = "SONG";
    public final static String UPDATE_POSITION_ACTION = "POSITION";

    private NotificationManagerCompat notificationManager;
    private MediaSessionCompat mediaSession;
    private MediaMetadataCompat.Builder mediaMetadataBuilder;
    private PlaybackStateCompat.Builder playbackStateBuilder;

    private boolean notificationStarted;

    /**
     * The methods in this callback handler will be called when a hardware device (like Bluetooth) triggers them.
     */
    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();

            PlayViewModel playViewModel = getPlayerViewModel();
            if (playViewModel != null) {
                playViewModel.playIfNotAlready();
            }
        }

        @Override
        public void onPause() {
            super.onPause();

            PlayViewModel playViewModel = getPlayerViewModel();
            if (playViewModel != null) {
                playViewModel.pauseIfNotAlready();
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);

            System.out.printf("onSeekTo %d\n", pos);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();

            PlayViewModel playViewModel = getPlayerViewModel();
            if (playViewModel != null) {
                playViewModel.nextSong();
            }
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            PlayViewModel playViewModel = getPlayerViewModel();
            if (playViewModel != null) {
                playViewModel.previousSong();
            }
        }

        private PlayViewModel getPlayerViewModel() {
            Player player = Player.getInstance();
            if (player != null) {
                return player.getViewModel();
            }

            return null;
        }
    };

    private boolean getIsPlaying() {
        Player player = Player.getInstance();
        return player != null && player.isPlaying();
    }

    private long getAvailableActions() {


        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (getIsPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();

        playbackStateBuilder = new PlaybackStateCompat.Builder();

        mediaMetadataBuilder = new MediaMetadataCompat.Builder();

        // Start a new MediaSession
        mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);

        setSessionToken(mediaSession.getSessionToken());
        mediaSession.setCallback(mediaSessionCallback);

//        Context context = getApplicationContext();
//        Intent intent = new Intent(context, NowPlayingActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mediaSession.setSessionActivity(pi);

        notificationStarted = false;

        createNotificationChannel();

        startNotification();
    }

    public void startNotification() {
        if (!notificationStarted) {
            // mMetadata = mController.getMetadata();
            //mPlaybackState = mController.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification(getIsPlaying());
            if (notification != null) {
                startForeground(NOTIFICATION_ID, notification);
                notificationStarted = true;
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = NotificationManagerCompat.from(this);

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Flumen Notification Channel", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setVibrationPattern(new long[]{ 0 });
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Notification createNotification(boolean isPlaying) {
        Intent nextSongIntentAction = new Intent(this, MediaActionReceiver.class);
        nextSongIntentAction.putExtra("action", "next");
        PendingIntent nextSongIntent = PendingIntent.getBroadcast(this,1, nextSongIntentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevSongIntentAction = new Intent(this, MediaActionReceiver.class);
        prevSongIntentAction.putExtra("action", "previous");
        PendingIntent prevSongIntent = PendingIntent.getBroadcast(this,2, prevSongIntentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playPauseIntentAction = new Intent(this, MediaActionReceiver.class);
        playPauseIntentAction.putExtra("action", "playPause");
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this,3, playPauseIntentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        androidx.core.app.NotificationCompat.Action playPauseAction;
        if (isPlaying) {
            playPauseAction = new androidx.core.app.NotificationCompat.Action.Builder(R.drawable.ic_pause_24dp, "Pause", playPauseIntent).build();
        } else {
            playPauseAction = new androidx.core.app.NotificationCompat.Action.Builder(R.drawable.ic_play_24dp, "Play", playPauseIntent).build();
        }

        Notification notification = new androidx.core.app.NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL)
                .setStyle(new NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()))
                .setSmallIcon(R.drawable.flumen_transparent)
                .addAction(R.drawable.ic_previous_24dp, "Previous", prevSongIntent)
                .addAction(playPauseAction)
                .addAction(R.drawable.ic_next_24dp, "Next", nextSongIntent)
                .build();

        return notification;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaItem>> result) {

    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            if (UPDATE_SONG_ACTION.equals(startIntent.getAction())) {
                String name = startIntent.getStringExtra("name");
                String artist = startIntent.getStringExtra("artist");
                String album = startIntent.getStringExtra("album");
                long duration = startIntent.getIntExtra("duration", 0);

                mediaSession.setMetadata(
                        mediaMetadataBuilder
                            // Title
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, name)
                            // Artist
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                            // Album
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)

                            // Duration.
                            // If duration isn't set, such as for live broadcasts, then the progress
                            // indicator won't be shown on the seekbar.
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration) //
                            .build()
                );

                notificationManager.notify(NOTIFICATION_ID, createNotification(getIsPlaying()));

            } else if (UPDATE_POSITION_ACTION.equals(startIntent.getAction())) {

                boolean isPlaying = startIntent.getBooleanExtra("isPlaying", false);
                long position = startIntent.getLongExtra("position", 0);

                mediaSession.setPlaybackState(playbackStateBuilder
                        .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, position, 0)
                        .setActions(getAvailableActions())
                        .build());

                notificationManager.notify(NOTIFICATION_ID, createNotification(isPlaying));
            }
        }

        return START_STICKY;
    }

    /**
     * (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        mediaSession.release();
    }

    public void onPlaybackStart() {
        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    public void onPlaybackStop() {

        stopForeground(true);
    }

//    @Override
//    public void onNotificationRequired() {
//        startNotification();
//    }
//
//    @Override
//    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
//        mediaSession.setPlaybackState(newState);
//    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        stopSelf();

        System.exit(0);
    }
}
