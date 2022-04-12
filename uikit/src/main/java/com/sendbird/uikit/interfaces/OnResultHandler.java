package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendBirdException;

/**
 * Interface definition for a callback to be invoked when getting a result.
 */
public interface OnResultHandler<T> {
    /**
     * Called when a result has been succeeded.
     *
     * @param result The object of result.
     */
    void onResult(@NonNull T result);

    /**
     * Called when a error has been invoked.
     *
     * @param e The object of exception.
     */
    void onError(@Nullable SendBirdException e);
}
