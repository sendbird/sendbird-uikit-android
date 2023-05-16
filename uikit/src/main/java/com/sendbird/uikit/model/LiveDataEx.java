package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.sendbird.uikit.log.Logger;

/**
 * Extension class for live data.
 *
 * @param <T> The type of data held by this instance
 * since 3.0.0
 */
public class LiveDataEx<T> extends LiveData<T> {
    public LiveDataEx(T value) {
        super(value);
    }

    public LiveDataEx() {
        super();
    }

    /**
     * Observes forever, however, remove the observer when ON_DESTROY is invoked.
     *
     * @param owner The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     * since 3.0.0
     */
    public void observeAlways(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        Logger.d(">> LiveDataEx::observeAlways()");
        this.observeForever(observer);
        owner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                this.removeObserver(observer);
            }
        });
    }
}
