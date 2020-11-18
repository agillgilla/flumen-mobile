package com.asgstudios.flumen_mobile;

import android.content.SharedPreferences;


public class Preferences {

    private static Preferences instance;

    private SharedPreferences preferences;

    private SharedPreferences.Editor preferencesEditor;

    public static Preferences getOrInstantiate(SharedPreferences preferences)
    {
        if (instance == null) {
            instance = new Preferences(preferences);
        }
        return instance;
    }

    public static Preferences getInstance()
    {
        return instance;
    }

    private Preferences(SharedPreferences preferences) {
        this.preferences = preferences;
        this.preferencesEditor = preferences.edit();
    }

    public int getLastPlaylistIndex() {
        return this.preferences.getInt("lastPlaylist", 0);
    }

    public void setLastPlaylistIndex(int playlistIndex) {
        this.preferencesEditor.putInt("lastPlaylist", playlistIndex);
        this.preferencesEditor.apply();
    }
}
