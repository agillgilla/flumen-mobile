package com.asgstudios.flumen_mobile.ui.sync;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SyncViewModel extends ViewModel {

    private MutableLiveData<String> syncStatusText;

    public SyncViewModel() {
        syncStatusText = new MutableLiveData<>();
        syncStatusText.setValue("Syncing...");
    }

    public LiveData<String> getText() {
        return syncStatusText;
    }
}