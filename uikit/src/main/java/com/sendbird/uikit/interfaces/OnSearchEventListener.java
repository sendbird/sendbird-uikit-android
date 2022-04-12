package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

/**
 * Callback interface to be invoked when searching a message.
 */
public interface OnSearchEventListener {
    /**
     * Called when requesting to search a message
     *
     * @param keyword Keyword to search for messages
     */
    void onSearchRequested(@NonNull String keyword);
}
