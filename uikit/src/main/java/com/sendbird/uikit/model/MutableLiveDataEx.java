package com.sendbird.uikit.model;

/**
 * LiveData which publicly exposes setValue(T) and postValue(T) method.
 *
 * @param <T> The type of data hold by this instance
 * since 3.0.0
 */
public class MutableLiveDataEx<T> extends LiveDataEx<T> {
    public MutableLiveDataEx(T value) {
        super(value);
    }

    public MutableLiveDataEx() {
        super();
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }
}
