package com.asgstudios.flumen_mobile.ui.play;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asgstudios.flumen_mobile.MainActivity;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.Song;
import com.asgstudios.flumen_mobile.SyncWorker;
import com.asgstudios.flumen_mobile.ui.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.asgstudios.flumen_mobile.SyncWorker.JSON_INDEX_FILENAME;

public class PlayFragment extends Fragment {

    private MainActivity mainActivity;
    private View rootView;

    private PlayViewModel playViewModel;

    private Player player;
    private PlayAdapter playAdapter;
    private RecyclerView recyclerView;

    private JSONObject indexJson;

    List<String> playlists;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        playViewModel =
                ViewModelProviders.of(this).get(PlayViewModel.class);
        View root = inflater.inflate(R.layout.fragment_play, container, false);


        //final TextView textView = root.findViewById(R.id.text_dashboard);
        playViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        ImageButton playButton = root.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //player.playSong();
                // TODO: Implement shuffle!

                player.playPause();
            }
        });

        this.rootView = root;

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mainActivity = (MainActivity) getParentFragment().getActivity();
        this.player = Player.getOrInstantiate(mainActivity);
    }

    @Override
    public void onStart() {
        super.onStart();

        File filesDir = mainActivity.getExternalFilesDir(null);
        File musicDir = new File(filesDir, SyncWorker.MUSIC_DIR);


        /*
        File[] playlistDirs = musicDir.listFiles();
        for (File playlistDir : playlistDirs) {
            if (playlistDir.isDirectory()) {
                File[] songs = playlistDir.listFiles();
                for (File song : songs) {
                    if (song.getName().endsWith(".mp3")) {
                        Mp3File mp3file = null;
                        try {
                            mp3file = new Mp3File(song);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (UnsupportedTagException e) {
                            e.printStackTrace();
                        } catch (InvalidDataException e) {
                            e.printStackTrace();
                        }
                        if (mp3file.hasId3v1Tag()) {
                            ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                            System.out.println("Track: " + id3v1Tag.getTrack());
                            System.out.println("Artist: " + id3v1Tag.getArtist());
                            System.out.println("Title: " + id3v1Tag.getTitle());
                            System.out.println("Album: " + id3v1Tag.getAlbum());
                            System.out.println("Length: " + mp3file.getLengthInSeconds());
                        }
                    }
                }
            }
        }
        */

        this.playlists =  new ArrayList<>();

        File[] playlistDirs = musicDir.listFiles();
        if (playlistDirs == null) {
            return;
        }
        for (File playlistDir : playlistDirs) {
            if (playlistDir.isDirectory()) {
                this.playlists.add(playlistDir.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.playlist_spinner_layout, playlists);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner playlistSpinner = (Spinner) mainActivity.findViewById(R.id.playlist_spinner);
        playlistSpinner.setAdapter(adapter);

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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        recyclerView = getView().findViewById(R.id.playView);
        if (recyclerView == null) {
            System.out.println("RECYCLER VIEW IS NULL!");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        updatePlaylist();

        playlistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updatePlaylist();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
    }

    public void updatePlaylist() {

        try {
            Spinner playlistSpinner = (Spinner) mainActivity.findViewById(R.id.playlist_spinner);

            player.setPlaylist(playlists.get(playlistSpinner.getSelectedItemPosition()));

            JSONArray playlistArray = indexJson.getJSONArray(playlists.get(playlistSpinner.getSelectedItemPosition()));

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

            this.playAdapter = new PlayAdapter(player, this.getContext(), songs);
            recyclerView.setAdapter(playAdapter);

            //System.out.println(indexJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}