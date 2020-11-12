package com.asgstudios.flumen_mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private static Vibrator vibrator;

    private NotificationManagerCompat notificationManager;
    private Notification mediaNotification;
    private NotificationCompat.Builder mediaNotificationBuilder;

    private PendingIntent playPauseIntent;

    private static final String NOTIFICATION_CHANNEL = "flumen_media";

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

        showMediaNotification();
    }

    public static void vibrate(int millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // Deprecated in API 26
            vibrator.vibrate(millis);
        }
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

        notificationManager.notify(1, mediaNotification);

        System.out.println("SHOWED NOTIFICATION!");
    }

    public void updateNotification(boolean isPlaying) {
        if (isPlaying) {
            mediaNotification.actions[1] = new Notification.Action(android.R.drawable.ic_media_pause, "Pause", playPauseIntent);
        } else {
            mediaNotification.actions[1] = new Notification.Action(android.R.drawable.ic_media_play, "Play", playPauseIntent);
        }

        notificationManager.notify(1, mediaNotification);
    }

    public void updateNotificationSong(Song song) {
        mediaNotificationBuilder.setContentTitle(song.getName());
        mediaNotificationBuilder.setContentText(song.getArtist());
        mediaNotification = mediaNotificationBuilder.build();

        notificationManager.notify(1, mediaNotification);
    }

    public void updateNotificationPlaylist(Playlist playlist) {
        mediaNotificationBuilder.setSubText(playlist.getPlaylistName());
        mediaNotification = mediaNotificationBuilder.build();

        notificationManager.notify(1, mediaNotification);
    }
}