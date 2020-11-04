package com.asgstudio.flumen_mobile.ui.sync;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SyncViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SyncViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}