package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 *
 * @param <T>
 * @since 3.0.0
 */
public final class SingleEventObserver<T> implements Observer<SingleEvent<T>> {
    @NonNull
    private final Observer<T> singleEventSubscriber;

    public SingleEventObserver(@NonNull Observer<T> singleEventSubscriber) {
        this.singleEventSubscriber = singleEventSubscriber;
    }

    @Override
    public void onChanged(@Nullable SingleEvent<T> singleEvent) {
        if (singleEvent != null) {
            T content = singleEvent.getContentIfNotHandled();
            if (content != null) this.singleEventSubscriber.onChanged(content);
        }
    }
}
