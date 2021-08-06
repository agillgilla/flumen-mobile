package com.asgstudios.flumen_mobile;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Player.getInstance().getViewModel().playIfNotAlready();
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Player.getInstance().getViewModel().pauseIfNotAlready();
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            final KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        // stop music
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        playPause();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        next();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        previous();
                        break;
                }
            }
        }
    }

    public void playPause() {
        Player.getInstance().getViewModel().playPause();
    }

    public void next() {
        Player.getInstance().getViewModel().nextSong();
    }

    public void previous() {
        Player.getInstance().getViewModel().previousSong();
    }
}
