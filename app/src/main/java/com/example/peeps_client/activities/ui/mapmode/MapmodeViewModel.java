package com.example.peeps_client.activities.ui.mapmode;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapmodeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MapmodeViewModel() {
//        mText = new MutableLiveData<>();
//        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}