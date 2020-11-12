package com.asgstudios.flumen_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getStringExtra("action");

        if (action.equals("playPause")) {
            playPause();
        } else if (action.equals("next")) {
            next();
        } else if (action.equals("previous")) {
            previous();
        }
    }

    public void playPause() {
        Player.getInstance().getViewModel().playPause();
    }

    public void next() {
        System.out.println("NEXT SONG!");
        //Player.getInstance().next();
    }

    public void previous() {
        System.out.println("PREVIOUS SONG!");
        //Player.getInstance().previous();
    }
}
