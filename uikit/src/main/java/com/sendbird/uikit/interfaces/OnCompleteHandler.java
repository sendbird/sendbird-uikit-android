package com.sendbird.uikit.interfaces;

import androidx.annotation.Nullable;

import com.sendbird.android.SendBirdException;

/**
 * Interface definition that delivers the results of the request.
 */
public interface OnCompleteHandler {
    /**
     * Called when request is finished.
     * The presence of a value of e delivered to the parameter is a case where the request fails.
     *
     * @param e The object of exception.
     * @since 3.0.0
     */
    void onComplete(@Nullable SendBirdException e);
}
