package com.asgstudios.flumen_mobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static Vibrator VIBRATOR;

    private static final String NOTIFICATION_CHANNEL = "flumen_media";
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VIBRATOR = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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

        sendOnChannel();
    }

    public static void vibrate(int millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VIBRATOR.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // Deprecated in API 26
            VIBRATOR.vibrate(millis);
        }
    }

    public void sendOnChannel() {
        //Bitmap icon = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.ic_launcher_foreground);

        //Bitmap icon = ((VectorDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_background, null));

        Bitmap icon = BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.ic_launcher_foreground);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.flumen_large_plain)
                .setContentTitle("Song Name")
                .setContentText("Artist")
                .setLargeIcon(icon)
                .addAction(android.R.drawable.ic_media_previous, "Previous", null)
                .addAction(android.R.drawable.ic_media_pause, "Pause", null)
                .addAction(android.R.drawable.ic_media_next, "Next", null)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().
                        setShowActionsInCompactView(0, 1, 2))
                .setSubText("Sub Text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notificationManager.notify(1, notification);
    }
}