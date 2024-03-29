package com.asgstudios.flumen_mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.renderscript.RenderScript;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private static Vibrator vibrator;

    private NotificationManagerCompat notificationManager;
    private Notification mediaNotification;
    private NotificationCompat.Builder mediaNotificationBuilder;

    private PendingIntent playPauseIntent;

    private static final String NOTIFICATION_CHANNEL = "flumen_media";

    private static final String AVRCP_PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    private static final String AVRCP_META_CHANGED = "com.android.music.metachanged";

    private BluetoothReceiver bluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_sync, R.id.navigation_play, R.id.navigation_settings).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setSelectedItemId(R.id.navigation_play);


        notificationManager = NotificationManagerCompat.from(this);

        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL, "Flumen Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);

        Preferences.getOrInstantiate(preferences);

        /*
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new NotificationCompat.Builder(this, NotifierService.ANDROID_CHANNEL_ID)
                        .setContentTitle("LocationNotifier")
                        .setContentText("LocationNotifier is running.")
                        .setSmallIcon(android.R.drawable.stat_notify_more)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().
                                setShowActionsInCompactView(0, 1, 2))
                        .setContentIntent(pendingIntent)
                        .setTicker("LocationNotifier ticker")
                        .build();

        startForegroundService(ONGOING_NOTIFICATION_ID, notification);
        */

        showMediaNotification();

        registerBluetoothReceiver();
    }

    public static void vibrate(int millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // Deprecated in API 26
            vibrator.vibrate(millis);
        }
    }

    public static void vibrateDouble(int millis) {
        long[] vibrationPattern = new long[4];
        for (int i = 0; i < 4; i++) {
            vibrationPattern[i] = millis * i;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1));
        } else {
            // Deprecated in API 26
            vibrator.vibrate(vibrationPattern, -1);
        }
    }

    public void registerBluetoothReceiver() {
        bluetoothReceiver = new BluetoothReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        filter.setPriority(Integer.MAX_VALUE);
        this.registerReceiver(bluetoothReceiver, filter);
    }

    public void showMediaNotification() {
        //Bitmap icon = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.ic_launcher_foreground);

        //Bitmap icon = ((VectorDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_background, null));

        Bitmap icon = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.ic_launcher_foreground);

        Intent playPauseIntentAction = new Intent(this, MediaActionReceiver.class);
        playPauseIntentAction.putExtra("action","playPause");
        playPauseIntent = PendingIntent.getBroadcast(this,1, playPauseIntentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntentAction = new Intent(this, MediaActionReceiver.class);
        nextIntentAction.putExtra("action","next");
        PendingIntent nextIntent = PendingIntent.getBroadcast(this,2, nextIntentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntentAction = new Intent(this, MediaActionReceiver.class);
        prevIntentAction.putExtra("action","previous");
        PendingIntent prevIntent = PendingIntent.getBroadcast(this,3, prevIntentAction, PendingIntent.FLAG_UPDATE_CURRENT);

         mediaNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.flumen_large_plain)
                .setContentTitle("")
                .setContentText("")
                .setLargeIcon(icon)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevIntent)
                .addAction(android.R.drawable.ic_media_pause, "Pause", playPauseIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().
                        setShowActionsInCompactView(0, 1, 2))
                .setSubText("")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L});

        mediaNotification = mediaNotificationBuilder.build();

        mediaNotification.flags = Notification.FLAG_ONGOING_EVENT;

        //notificationManager.notify(1, mediaNotification);

//        Intent startIntent = new Intent(this, NotificationService.class);
//        startIntent.setAction(NotificationService.START_ACTION);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(startIntent);
//        } else {
//            startService(startIntent);
//        }

        Intent startIntent = new Intent(this, MusicService.class);
        //startIntent.setAction(NotificationService.START_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
    }

    public void updateNotificationPlayingPosition(boolean isPlaying, long position) {
        Intent updateIntent = new Intent(this, MusicService.class);
        updateIntent.setAction(MusicService.UPDATE_POSITION_ACTION);
        updateIntent.putExtra("isPlaying", isPlaying);
        updateIntent.putExtra("position", position);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(updateIntent);
        } else {
            startService(updateIntent);
        }
    }

    public void updateNotificationPlaying(boolean isPlaying) {
//        if (isPlaying) {
//            mediaNotification.actions[1] = new Notification.Action(android.R.drawable.ic_media_pause, "Pause", playPauseIntent);
//        } else {
//            mediaNotification.actions[1] = new Notification.Action(android.R.drawable.ic_media_play, "Play", playPauseIntent);
//        }
//
//        Intent updateIntent = new Intent(this, NotificationService.class);
//        updateIntent.setAction(NotificationService.UPDATE_PLAYING_ACTION);
//        updateIntent.putExtra("playing", isPlaying);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(updateIntent);
//        } else {
//            startService(updateIntent);
//        }

        //notificationManager.notify(1, mediaNotification);
    }

    public void updateNotificationSong(Song song) {
//        mediaNotificationBuilder.setContentTitle(song.getName());
//        mediaNotificationBuilder.setContentText(song.getArtist());
//        mediaNotification = mediaNotificationBuilder.build();
//
//        mediaNotification.flags = Notification.FLAG_ONGOING_EVENT;
//
//        Intent updateIntent = new Intent(this, NotificationService.class);
//        updateIntent.setAction(NotificationService.UPDATE_SONG_ACTION);
//        updateIntent.putExtra("name", song.getName());
//        updateIntent.putExtra("artist", song.getArtist());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(updateIntent);
//        } else {
//            startService(updateIntent);
//        }

        //notificationManager.notify(1, mediaNotification);

        Intent updateIntent = new Intent(this, MusicService.class);
        updateIntent.setAction(MusicService.UPDATE_SONG_ACTION);
        updateIntent.putExtra("name", song.getName());
        updateIntent.putExtra("artist", song.getArtist());
        updateIntent.putExtra("album", song.getAlbum());
        updateIntent.putExtra("duration", song.getLength());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(updateIntent);
        } else {
            startService(updateIntent);
        }
    }

    public void updateNotificationPlaylist(Playlist playlist) {
//        mediaNotificationBuilder.setSubText(playlist.getPlaylistName());
//        mediaNotification = mediaNotificationBuilder.build();
//
//        mediaNotification.flags = Notification.FLAG_ONGOING_EVENT;
//
//        Intent updateIntent = new Intent(this, NotificationService.class);
//        updateIntent.setAction(NotificationService.UPDATE_PLAYLIST_ACTION);
//        updateIntent.putExtra("playlist", playlist.getPlaylistName());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(updateIntent);
//        } else {
//            startService(updateIntent);
//        }

        //notificationManager.notify(1, mediaNotification);


    }

    public long md5(String s) {
        long value = 0;
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // This will overflow the long, since MD5 hashes are 16 bytes and longs are only 4 bytes, but that's okay

            for (int i = 0; i < messageDigest.length; i++)
            {
                value += ((long) messageDigest[i] & 0xffL) << (8 * i);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return value;
    }

    public void updateSongBluetooth(Song song, boolean playing) {
        Intent i = new Intent(AVRCP_META_CHANGED);
        i.putExtra("id", md5(song.getFilename()));
        i.putExtra("artist", song.getArtist());
        i.putExtra("album", song.getAlbum());
        i.putExtra("track", song.getName());
        i.putExtra("playing", playing);
        i.putExtra("ListSize", 1);
        i.putExtra("duration", song.getLength());
        i.putExtra("position", Player.getInstance().getCurrTimeMillis() / 1000f);
        sendBroadcast(i);
    }

    public void updateIsPlayingBluetooth(Song song, boolean playing) {
        Intent i = new Intent(AVRCP_PLAYSTATE_CHANGED);
        i.putExtra("id", md5(song.getFilename()));
        i.putExtra("artist", song.getArtist());
        i.putExtra("album", song.getAlbum());
        i.putExtra("track", song.getName());
        i.putExtra("playing", playing);
        i.putExtra("ListSize", 1);
        i.putExtra("duration", song.getLength());
        i.putExtra("position", Player.getInstance().getCurrTimeMillis() / 1000f);
        sendBroadcast(i);
    }
}