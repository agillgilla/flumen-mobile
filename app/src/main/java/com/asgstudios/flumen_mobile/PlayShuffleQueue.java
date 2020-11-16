package com.asgstudios.flumen_mobile;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayShuffleQueue {

    private static PlayShuffleQueue instance;

    private Playlist activePlaylist;

    private HashMap<Playlist, List<Song>> playlistSongs;

    /**
     * Queue indices always point at the next song to play in the list (not the one we are currently playing.)
     */
    private HashMap<Playlist, Integer> playlistQueueIndices;

    private HashMap<Playlist, List<Integer>> playlistQueueShuffleIndices;

    public static PlayShuffleQueue getOrInstantiate(PlaylistManager playlistManager)
    {
        if (instance == null) {
            instance = new PlayShuffleQueue(playlistManager);
        }
        return instance;
    }

    public static PlayShuffleQueue getInstance()
    {
        return instance;
    }

    private PlayShuffleQueue(PlaylistManager playlistManager) {
        playlistSongs = new HashMap<>();
        playlistQueueIndices = new HashMap<>();
        playlistQueueShuffleIndices = new HashMap<>();

        for (Playlist playlist : playlistManager.getPlaylists()) {
            playlistSongs.put(playlist, playlistManager.getPlaylistSongs(playlist, true));
            playlistQueueIndices.put(playlist, 0);
        }

        this.setActivePlaylist(playlistManager.getPlaylists().get(0));
    }

    public void setActivePlaylist(Playlist playlist) {
        this.activePlaylist = playlist;

        if (playlistQueueShuffleIndices.get(playlist) == null) {
            List<Integer> shuffleIndices = new ArrayList<>();

            System.out.println("Setting active playlist: " + playlist.getPlaylistName());

            List<Song> playlistSongList = playlistSongs.get(playlist);
            if (playlistSongList != null) {
                for (int i = 0; i < playlistSongs.get(playlist).size(); i++) {
                    shuffleIndices.add(i);
                }
            }

            Collections.shuffle(shuffleIndices);
            playlistQueueShuffleIndices.put(playlist, shuffleIndices);
        }
    }

    public Song nextSong() {
        int currPlaylistIndex = playlistQueueIndices.get(activePlaylist);

        List<Integer> playlistShuffleIndices = playlistQueueShuffleIndices.get(activePlaylist);
        int playlistShuffleIndex = playlistShuffleIndices.get(currPlaylistIndex);
        Song song = playlistSongs.get(activePlaylist).get(playlistShuffleIndex);

        currPlaylistIndex++;
        if (currPlaylistIndex >= playlistShuffleIndices.size()) {
            currPlaylistIndex = 0;
            shuffleActivePlaylist();
        }
        playlistQueueIndices.put(activePlaylist, currPlaylistIndex);

        return song;
    }

    public Song previousSong() {
        int currPlaylistIndex = playlistQueueIndices.get(activePlaylist);

        int prevPlaylistIndex = Math.max(0, currPlaylistIndex - 2);

        currPlaylistIndex = prevPlaylistIndex + 1;

        Song song = playlistSongs.get(activePlaylist).get(playlistQueueShuffleIndices.get(activePlaylist).get(prevPlaylistIndex));

        playlistQueueIndices.put(activePlaylist, currPlaylistIndex);

        return song;
    }

    public void shuffleActivePlaylist() {
        Collections.shuffle(playlistQueueShuffleIndices.get(activePlaylist));
    }

    public int peekNextSongIndex() {
        List<Integer> currPlaylistShuffleIndices = playlistQueueShuffleIndices.get(activePlaylist);
        int currPlaylistQueueIndex = playlistQueueIndices.get(activePlaylist);

        return currPlaylistShuffleIndices.get(currPlaylistQueueIndex);
    }

    public int peekPreviousSongIndex() {
        int currPlaylistIndex = playlistQueueIndices.get(activePlaylist);
        int prevPlaylistIndex = Math.max(0, currPlaylistIndex - 2);

        return playlistQueueShuffleIndices.get(activePlaylist).get(prevPlaylistIndex);
    }
}
