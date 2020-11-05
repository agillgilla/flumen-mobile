package com.asgstudios.flumen_mobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class SyncManager {
    private MainActivity mainActivity;
    private Handler statusHandler;
    private SyncWorker syncWorker;

    public SyncManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        final MainActivity activity = mainActivity;

        this.statusHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                final String string = bundle.getString("status");
                final TextView myTextView = (TextView)activity.findViewById(R.id.syncStatus);
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        myTextView.setText(string);
                    }
                });
            }
        };
        this.syncWorker = new SyncWorker(mainActivity, statusHandler);

        System.out.println("FILES PATH: " + mainActivity.getFilesDir().getAbsolutePath());
        System.out.println("ALTERNATE PATH: " + mainActivity.getExternalFilesDir(null));
    }

    public void sync() {
        Thread syncThread = new Thread(syncWorker);
        syncThread.start();
    }
}

/*
public class ScanHostsTask implements Runnable {

    private CommandSender commandSender;
    private String host;
    private boolean isConnected;

    private final static int timeout = 3000;

    public ScanHostsTask(CommandSender commandSender, String host) {
        this.commandSender = commandSender;
        this.host = host;
        this.isConnected = false;
    }

    @Override
    public void run() {
        Optional<Long> empty = Optional.empty();
        this.isConnected = commandSender.scanHostsAndConnect(Arrays.asList(host), empty);
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}
 */
