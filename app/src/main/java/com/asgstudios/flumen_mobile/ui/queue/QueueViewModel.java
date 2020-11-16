package com.asgstudios.flumen_mobile.ui.queue;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.asgstudios.flumen_mobile.PlayQueue;
import com.asgstudios.flumen_mobile.SongAndIndex;

import java.util.List;

public class QueueViewModel extends ViewModel {

    private QueueAdapter queueAdapter;

    private PlayQueue playQueue;

    private MutableLiveData<List<SongAndIndex>> queue;

    public QueueViewModel() {
        this.playQueue = PlayQueue.getOrInstantiate();
        this.playQueue.setViewModel(this);

        this.queue = new MutableLiveData<>();
        this.queue.setValue(playQueue.getQueue());
    }

    public MutableLiveData<List<SongAndIndex>> getQueue() {
        return queue;
    }

    public void setQueue(List<SongAndIndex> queue) {
        this.queue.setValue(queue);
    }

    public PlayQueue getPlayQueue() {
        return playQueue;
    }

    public void setQueueAdapter(QueueAdapter queueAdapter) {
        this.queueAdapter = queueAdapter;
    }

    public QueueAdapter getQueueAdapter() {
        return queueAdapter;
    }
}