package com.asgstudios.flumen_mobile;

import com.asgstudios.flumen_mobile.ui.queue.QueueViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlayQueue {

    private static PlayQueue instance;

    private List<SongAndIndex> songQueue;

    private QueueViewModel queueViewModel;

    public static PlayQueue getOrInstantiate()
    {
        if (instance == null) {
            instance = new PlayQueue();
        }
        return instance;
    }

    public static PlayQueue getInstance()
    {
        return instance;
    }

    public PlayQueue() {
        this.songQueue = new ArrayList<>();
    }

    public List<SongAndIndex> getQueue() {
        return this.songQueue;
    }

    public boolean isEmpty() {
        return this.songQueue.isEmpty();
    }

    public void push(Song song, int index) {
        this.songQueue.add(new SongAndIndex(song, index));

        updateViewModelQueue();
    }

    public void moveUp(int songIndex) {
        if (songQueue.size() > 1 && songIndex > 0) {
            SongAndIndex songToMoveDown = songQueue.get(songIndex - 1);
            songQueue.set(songIndex - 1, songQueue.get(songIndex));
            songQueue.set(songIndex, songToMoveDown);

            updateViewModelQueue();
        }
    }

    public void moveDown(int songIndex) {
        if (songQueue.size() > 1 && songIndex < songQueue.size() - 1) {
            SongAndIndex songToMoveUp = songQueue.get(songIndex + 1);
            songQueue.set(songIndex + 1, songQueue.get(songIndex));
            songQueue.set(songIndex, songToMoveUp);

            updateViewModelQueue();
        }
    }

    public SongAndIndex pop() {
        SongAndIndex songAndIndex  = songQueue.get(0);
        songQueue.remove(0);

        updateViewModelQueue();

        return songAndIndex;
    }

    public SongAndIndex peek() {
        return songQueue.get(0);
    }

    public void remove(int songIndex) {
        this.songQueue.remove(songIndex);

        updateViewModelQueue();
    }

    public void setViewModel(QueueViewModel queueViewModel) {
        this.queueViewModel = queueViewModel;
    }

    public void updateViewModelQueue() {
        if (queueViewModel != null) {
            queueViewModel.setQueue(songQueue);
        }
    }
}
