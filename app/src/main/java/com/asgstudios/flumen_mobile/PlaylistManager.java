package com.asgstudios.flumen_mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.asgstudios.flumen_mobile.SyncWorker.JSON_INDEX_FILENAME;

public class PlaylistManager {

    private File filesDir;
    private JSONObject indexJson;

    List<Playlist> playlists;

    public PlaylistManager(File filesDir) {
        this.filesDir = filesDir;
    }

    public List<Playlist> loadPlaylistList() {
        //File filesDir = mainActivity.getExternalFilesDir(null);
        File musicDir = new File(filesDir, SyncWorker.MUSIC_DIR);

        this.playlists =  new ArrayList<>();

        File[] playlistDirs = musicDir.listFiles();
        if (playlistDirs == null) {
            return playlists;
        }
        int playlistIndex = 0;
        for (File playlistDir : playlistDirs) {
            if (playlistDir.isDirectory()) {
                this.playlists.add(new Playlist(playlistDir.getName(), playlistIndex));
                playlistIndex++;
            }
        }

        File jsonIndexFile = new File(filesDir, JSON_INDEX_FILENAME);
        StringBuilder out = new StringBuilder();
        try {
            int bufferSize = 1024;
            char[] buffer = new char[bufferSize];
            Reader in = new InputStreamReader(new FileInputStream(jsonIndexFile), Charset.forName("UTF-8"));
            int charsRead;
            while ((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                out.append(buffer, 0, charsRead);
            }

            this.indexJson = new JSONObject(out.toString());

            in.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JSONException jse) {
            jse.printStackTrace();
        }

        return this.playlists;
    }

    public List<Song> getPlaylistSongs(Playlist playlist) {
        try {
            JSONArray playlistArray = indexJson.getJSONArray(playlist.getPlaylistName());

            System.out.println(playlistArray);
            int playlistLength = playlistArray.length();

            List<Song> songs = new ArrayList<>(playlistLength);

            for (int i = 0; i < playlistLength; i++) {
                JSONObject playlistObj = playlistArray.getJSONObject(i);

                songs.add(new Song(playlistObj.getString("title"),
                        playlistObj.getString("artist"),
                        (int) Math.floor(playlistObj.getDouble("duration")),
                        playlistObj.getString("file")));
            }

            return songs;
        } catch (JSONException jse) {
            jse.printStackTrace();
        }

        return null;
    }
}
