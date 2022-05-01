package com.asgstudios.flumen_mobile.ui.play;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asgstudios.flumen_mobile.MainActivity;
import com.asgstudios.flumen_mobile.PlaybackInfo;
import com.asgstudios.flumen_mobile.Playlist;
import com.asgstudios.flumen_mobile.R;
import com.asgstudios.flumen_mobile.Song;

import java.util.ArrayList;
import java.util.List;


public class PlayFragment extends Fragment {

    private MainActivity mainActivity;
    private View rootView;

    private PlayViewModel playViewModel;

    private PlayAdapter playAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.mainActivity = (MainActivity) getParentFragment().getActivity();

        setRetainInstance(true);

        //playViewModel = ViewModelProviders.of(this).get(PlayViewModel.class);
        playViewModel = new ViewModelProvider(mainActivity, ViewModelProvider.AndroidViewModelFactory.getInstance(mainActivity.getApplication())).get(PlayViewModel.class);

        //View root = inflater.inflate(R.layout.fragment_play, container, false);

        // Got idea from https://stackoverflow.com/questions/54581071/fragments-destroyed-recreated-with-jetpacks-android-navigation-components
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_play, container, false);
        }

        playViewModel.getCurrSongTime().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String currSongTime) {
                TextView currTimeTextView = rootView.findViewById(R.id.currTimeTextView);
                currTimeTextView.setText(currSongTime);
            }
        });

        playViewModel.getCurrSongDuration().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String currSongDuration) {
                TextView durationTextView = rootView.findViewById(R.id.durationTextView);
                durationTextView.setText(currSongDuration);
            }
        });

        final SeekBar seekBar = rootView.findViewById(R.id.seekBar);

        playViewModel.getCurrSongPlaybackInfo().observe(getViewLifecycleOwner(), new Observer<PlaybackInfo>() {
            @Override
            public void onChanged(PlaybackInfo currSongPlaybackInfo) {
                seekBar.setMax((int) (currSongPlaybackInfo.getCurrSongDuration() / 100.0f));
                seekBar.setProgress((int) (currSongPlaybackInfo.getCurrSongTime() / 100.0f));
            }
        });

        final TextView playingSongTextView = rootView.findViewById(R.id.playingSongTextView);
        final TextView playingArtistTextView = rootView.findViewById(R.id.playingArtistTextView);
        // We observe forever because otherwise the notification wouldn't update when the app has lost focus
        playViewModel.getCurrSong().observeForever(new Observer<Song>() {
            @Override
            public void onChanged(Song currSong) {

                playingSongTextView.setText(currSong.getName());
                playingArtistTextView.setText(currSong.getArtist());

                mainActivity.updateNotificationSong(currSong);
                
                if (playViewModel.getCurrPlaylist().getValue() != null) {
                    mainActivity.updateNotificationPlaylist(playViewModel.getCurrPlaylist().getValue());
                }

                mainActivity.updateSongBluetooth(currSong, playViewModel.getIsPlaying().getValue());
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playViewModel.finalizeSeek(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playViewModel.beginSeek();
            }


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playViewModel.setCurrSongTime((int) (progress * 100.0f));
                }
            }
        });

        final Spinner playlistSpinner = (Spinner) rootView.findViewById(R.id.playlist_spinner);

        playViewModel.getPlaylists().observe(getViewLifecycleOwner(), new Observer<List<Playlist>>() {
            @Override
            public void onChanged(List<Playlist> playlists) {
                List<String> playlistNames = new ArrayList<>();
                for (Playlist playlist : playlists) {
                    playlistNames.add(playlist.getPlaylistName());
                }

                ArrayAdapter<String> playlistAdapter = new ArrayAdapter<>(rootView.getContext(), R.layout.playlist_spinner_layout, playlistNames);
                playlistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                playlistSpinner.setAdapter(playlistAdapter);
                //playlistAdapter.notifyDataSetChanged();
            }
        });

        playViewModel.getCurrPlaylist().observe(getViewLifecycleOwner(), new Observer<Playlist>() {
            @Override
            public void onChanged(Playlist currPlaylist) {
                if (currPlaylist != null) {
                    playlistSpinner.setSelection(currPlaylist.getPlaylistIndex());
                }
            }
        });

        playlistSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            int itemSelectedCount = 0;

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                itemSelectedCount++;

                if (itemSelectedCount <= 1) {
                    return;
                }

                //System.out.println("Setting playlist index from spinner: " + playlistSpinner.getSelectedItemPosition());
                playViewModel.setPlaylistIndex(playlistSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        final ImageButton playButton = rootView.findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(50);

                playViewModel.playPause();

                mainActivity.updateNotificationPlaying(playViewModel.getIsPlaying().getValue());

                mainActivity.updateIsPlayingBluetooth(playViewModel.getCurrSong().getValue(), playViewModel.getIsPlaying().getValue());
            }
        });

        final ImageButton nextButton = rootView.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(50);

                playViewModel.nextSong();

                mainActivity.updateNotificationPlaying(playViewModel.getIsPlaying().getValue());

                mainActivity.updateIsPlayingBluetooth(playViewModel.getCurrSong().getValue(), playViewModel.getIsPlaying().getValue());
            }
        });

        final ImageButton prevButton = rootView.findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(50);

                playViewModel.previousSong();

                mainActivity.updateNotificationPlaying(playViewModel.getIsPlaying().getValue());

                mainActivity.updateIsPlayingBluetooth(playViewModel.getCurrSong().getValue(), playViewModel.getIsPlaying().getValue());
            }
        });

        final ImageButton loopButton = rootView.findViewById(R.id.loopButton);
        loopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(25);
                playViewModel.setPlayMode(PlayViewModel.PlayMode.LOOP);
            }
        });

        final ImageButton shuffleButton = rootView.findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.vibrate(25);
                playViewModel.setPlayMode(PlayViewModel.PlayMode.SHUFFLE);
            }
        });

        playViewModel.getPlayMode().observe(getViewLifecycleOwner(), new Observer<PlayViewModel.PlayMode>() {
            @Override
            public void onChanged(PlayViewModel.PlayMode playMode) {
                if (playMode == PlayViewModel.PlayMode.LOOP) {
                    loopButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
                } else if (playMode == PlayViewModel.PlayMode.SHUFFLE) {
                    shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    loopButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorWhite)));
                }
            }
        });

        // We observe forever because otherwise the notification wouldn't update when the app has lost focus
        playViewModel.getIsPlaying().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPlaying) {
                if (isPlaying) {
                    playButton.setImageResource(R.drawable.ic_pause_24dp);
                } else {
                    playButton.setImageResource(R.drawable.ic_play_24dp);
                }
                mainActivity.updateNotificationPlaying(isPlaying);

                mainActivity.updateIsPlayingBluetooth(playViewModel.getCurrSong().getValue(), playViewModel.getIsPlaying().getValue());
            }
        });


        recyclerView = rootView.findViewById(R.id.playView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        playViewModel.getSongs().observe(getViewLifecycleOwner(), new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                playAdapter = new PlayAdapter(playViewModel, rootView.getContext(), songs, -1);
                playViewModel.setPlayAdapter(playAdapter);
                recyclerView.setAdapter(playAdapter);

                if (playViewModel.getPlayingPlaylist() != null &&
                        playViewModel.getPlayingPlaylist().getPlaylistIndex() == playlistSpinner.getSelectedItemPosition()) {

                    playAdapter.playingIndex = playViewModel.getPlayingIndex().getValue();
                    System.out.println("Set playingIndex to: " + playAdapter.playingIndex);
                    playAdapter.notifyDataSetChanged();
                } else {
                    System.out.println("Leaving playingIndex at: " + playAdapter.playingIndex);
                }
            }
        });

        // Hack to retain the currently selected playlist
        playViewModel.setCurrPlaylist(playViewModel.getCurrPlaylist().getValue());

        if (playAdapter != null) {
            playAdapter.notifyDataSetChanged();
        }

        /*
        if (playViewModel.getPlaylist() != null &&
                playViewModel.getPlaylist().getPlaylistIndex() != playlistSpinner.getSelectedItemPosition()) {

            System.out.println("Setting playlist index to: "+ playViewModel.getPlaylist().getPlaylistIndex());

            playlistSpinner.setSelection(playViewModel.getPlaylist().getPlaylistIndex());
            playViewModel.setPlaylistIndex(playlistSpinner.getSelectedItemPosition());
        }
        */

        return rootView;
    }


    /*
    public void updateExistingPlaylist(String playlistName) {
        Spinner playlistSpinner = (Spinner) mainActivity.findViewById(R.id.playlist_spinner);

        int playlistIndex = 0;
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).equals(playlistName)) {
                playlistIndex = i;
                break;
            }
        }

        playlistSpinner.setSelection(playlistIndex);

        loadPlaylist(false);
    }

    public void updateSelectedPlaylist() {
        Spinner playlistSpinner = (Spinner) mainActivity.findViewById(R.id.playlist_spinner);

        playViewModel.setPlaylist(playlists.get(playlistSpinner.getSelectedItemPosition()));

        loadPlaylist(true);
    }

    private void loadPlaylist(boolean createAdapter) {
        try {
            Spinner playlistSpinner = (Spinner) mainActivity.findViewById(R.id.playlist_spinner);

            if (createAdapter) {


                this.playAdapter = new PlayAdapter(playViewModel, this.getContext(), songs, -1);
                recyclerView.setAdapter(playAdapter);
                this.player.setPlayAdapter(playAdapter);
            } else {
                this.playAdapter = player.getPlayAdapter();
                recyclerView.setAdapter(playAdapter);
                playAdapter.notifyItemChanged(playAdapter.playingIndex);
                System.out.println("Playing index: " + playAdapter.playingIndex);
            }

            //System.out.println(indexJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    */
}