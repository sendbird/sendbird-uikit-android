package com.sendbird.uikit.interfaces;

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
    void onResult(T result);

    /**
     * Called when a error has been invoked.
     *
     * @param e The object of exception.
     */
    void onError(SendBirdException e);
}
