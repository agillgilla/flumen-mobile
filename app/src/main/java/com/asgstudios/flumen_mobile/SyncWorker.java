package com.asgstudios.flumen_mobile;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SyncWorker implements Runnable {
    private static final String SERVER_IP = "192.168.0.35";
    private static final String API_URI = "api";
    private static final String FETCH_PLAYLISTS_URI = "fetchPlaylists";
    private static final String SONGS_LIST_URI = "songsList";
    private static final String FETCH_SONG_URI = "fetchSong";

    private static final String MUSIC_DIR = "music";

    private MainActivity mainActivity;
    private Handler statusHandler;

    public SyncWorker(MainActivity mainActivity, Handler statusHandler) {
        this.mainActivity = mainActivity;
        this.statusHandler = statusHandler;
    }

    private static String readStringFromInputStream(InputStream inputStream) {
        try {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            int charsRead;
            while ((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                out.append(buffer, 0, charsRead);
            }

            return out.toString();
        } catch (IOException ioe) {
            System.out.println("IO Exception when reading input stream: " + ioe.getMessage());
            return null;
        }
    }

    private static Uri createApiGetRequest(String endPointFunction, String[] queryKeys, String[] queryVals) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.path(SERVER_IP);
        uriBuilder.scheme("http");
        uriBuilder.appendPath(API_URI);
        uriBuilder.appendPath(endPointFunction);

        if (queryKeys != null && queryVals != null) {
            for (int i = 0; i < Math.min(queryKeys.length, queryVals.length); i++) {
                uriBuilder.appendQueryParameter(queryKeys[i], queryVals[i]);
            }
        }

        return uriBuilder.build();
    }

    @Override
    public void run() {
        HttpURLConnection urlConnection = null;
        try {
            Uri playListsGetRequest =  createApiGetRequest(FETCH_PLAYLISTS_URI, null, null);
            URL playListsUrl = new URL(playListsGetRequest.toString());
            urlConnection = (HttpURLConnection) playListsUrl.openConnection();

            InputStream playlistsInputStream = new BufferedInputStream(urlConnection.getInputStream());
            String playListGetResponse = readStringFromInputStream(playlistsInputStream);

            ArrayList<String> playlistNames = new ArrayList<>();
            try {
                JSONArray playListJsonArray = new JSONArray(playListGetResponse);

                for (int i = 0; i < playListJsonArray.length(); i++) {
                    JSONObject playlistObj = playListJsonArray.getJSONObject(i);
                    playlistNames.add(playlistObj.getString("name"));
                }

                String[] playListQueryKey = new String[] {"playlist"};

                String[] songQueryKeys = new String[] {"playlist", "filename"};

                //File filesDir = mainActivity.getFilesDir();
                File filesDir = mainActivity.getExternalFilesDir(null);
                File musicDir = new File(filesDir, MUSIC_DIR);

                for (String playlistName : playlistNames) {
                    File playlistDir = new File(musicDir, playlistName);
                    if (!playlistDir.isDirectory()) {
                        playlistDir.mkdirs();
                    }

                    Uri songListGetRequest =  createApiGetRequest(SONGS_LIST_URI, playListQueryKey, new String[] {playlistName});
                    URL songListUrl = new URL(songListGetRequest.toString());
                    urlConnection = (HttpURLConnection) songListUrl.openConnection();

                    InputStream songListInputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String songListGetResponse = readStringFromInputStream(songListInputStream);

                    JSONArray songListJsonArray = new JSONArray(songListGetResponse);

                    int numSongs = songListJsonArray.length();

                    Message msg = statusHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("status", "Fetching playlist \"" + playlistName + "\"");
                    msg.setData(bundle);
                    statusHandler.dispatchMessage(msg);

                    for (int i = 0; i < songListJsonArray.length(); i++) {
                        JSONObject songFileObj = songListJsonArray.getJSONObject(i);
                        String songFilename = songFileObj.getString("file");

                        File songFilepath = new File(playlistDir, songFilename);
                        if (!songFilepath.exists()) {
                            // TODO: PULL SONG

                            System.out.println("Pulling song: " + songFilename);

                            Uri songGetRequest =  createApiGetRequest(FETCH_SONG_URI, songQueryKeys, new String[] {playlistName, songFilename});
                            URL songUrl = new URL(songGetRequest.toString());
                            urlConnection = (HttpURLConnection) songUrl.openConnection();

                            InputStream songInputStream = new BufferedInputStream(urlConnection.getInputStream());

                            OutputStream songOutputStream = new FileOutputStream(songFilepath);

                            byte[] buf = new byte[8192];
                            int length;
                            while ((length = songInputStream.read(buf)) > 0) {
                                songOutputStream.write(buf, 0, length);
                            }
                            songInputStream.close();
                            songOutputStream.close();
                        }

                        //System.out.println("DURATION: " + songFileObj.getDouble("duration"));
                    }


                }


            } catch (JSONException jse) {
                System.out.println("JSON Exception: " + jse.getMessage());
            }
        }
        catch (IOException ioe) {
            System.out.println("IO Exception: " + ioe.getMessage());
        } finally {
            urlConnection.disconnect();
        }
    }
}
