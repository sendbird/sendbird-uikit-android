package com.sendbird.uikit.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {
    protected final MutableLiveData<Integer> errorToast = new MutableLiveData<>();

    BaseViewModel() {}

    public LiveData<Integer> getErrorToast() {
        return errorToast;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
