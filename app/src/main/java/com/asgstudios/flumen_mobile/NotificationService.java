package com.asgstudios.flumen_mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationService extends Service {

    public static final String ANDROID_CHANNEL_ID = "com.asgstudios.flumen_mobile.ANDROID";

    private static final int ONGOING_NOTIFICATION_ID = 42;

    private static NotificationManagerCompat notificationManager;
    private static NotificationCompat.Builder notificationBuilder;
    private static Notification notification;

    private static PendingIntent playPauseIntent;

    public final static String START_ACTION = "START";
    public final static String UPDATE_PLAYING_ACTION = "PLAYING";
    public final static String UPDATE_SONG_ACTION = "SONG";
    public final static String UPDATE_PLAYLIST_ACTION = "PLAYLIST";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        notificationManager = NotificationManagerCompat.from(this);

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

        notificationBuilder = new NotificationCompat.Builder(this, NotificationService.ANDROID_CHANNEL_ID)
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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setVibrate(new long[]{0L});

        //notificationManager.notify(1, mediaNotification);

        Log.d("ONCREATE", "onCreate");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*
            CharSequence name = "Flumen";
            String description = "Facilitates user control of media player when minimized.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ANDROID_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            */


            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = NotificationManagerCompat.from(this);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(ANDROID_CHANNEL_ID, "Flumen Notification Channel", NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setVibrationPattern(new long[]{ 0 });
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ONSTART", "onStartCommand");

        if (intent == null || intent.getAction() == null) {
            Log.d("NULL_INTENT", "onStartCommand for NotifierService passed a null intent");
            stopForeground(true);
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        if (intent.getAction().equals(NotificationService.START_ACTION)) {
            Log.i("ONSTART", "Received Start Foreground Intent ");

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            notification = notificationBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;

            startForeground(ONGOING_NOTIFICATION_ID, notification);
        } else if (intent.getAction().equals(NotificationService.UPDATE_PLAYING_ACTION)) {
            boolean isPlaying = intent.getBooleanExtra("playing", false);
            if (isPlaying) {
                notification.actions[1] = new Notification.Action(android.R.drawable.ic_media_pause, "Pause", playPauseIntent);
            } else {
                notification.actions[1] = new Notification.Action(android.R.drawable.ic_media_play, "Play", playPauseIntent);
            }

            notification.flags = Notification.FLAG_ONGOING_EVENT;

            notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);

        } else if (intent.getAction().equals(NotificationService.UPDATE_SONG_ACTION)) {
            String name = intent.getStringExtra("name");
            String artist = intent.getStringExtra("artist");

            notificationBuilder.setContentTitle(name);
            notificationBuilder.setContentText(artist);
            notification = notificationBuilder.build();

            notification.flags = Notification.FLAG_ONGOING_EVENT;

            notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
        } else if (intent.getAction().equals(NotificationService.UPDATE_PLAYLIST_ACTION)) {
            String playlist = intent.getStringExtra("playlist");

            notificationBuilder.setSubText(playlist);
            notification = notificationBuilder.build();

            notification.flags = Notification.FLAG_ONGOING_EVENT;

            notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
        }

        return Service.START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        stopSelf();

        System.exit(0);
    }
}
