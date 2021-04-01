package com.sendbird.uikit.interfaces;

import com.sendbird.android.SendBirdException;

import java.util.List;

/**
 * Interface definition for a callback to be invoked when getting a result.
 *
 * @since 1.2.0
 */
public interface OnListResultHandler<T> {
    /**
     * Called when a result has been completed.
     *
     * @param result The object of result.
     */
    void onResult(List<T> result, SendBirdException e);
}
