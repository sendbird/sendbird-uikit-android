package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseMessage;

/**
 * Interface definition for a callback to be invoked when the message is filtered.
 */
public interface OnFilteringMessageHandler {
    /**
     * Called when sending message was filtered.
     *
     * @param filteredMessage A filtered message.
     */
    void onFiltered(@NonNull BaseMessage filteredMessage);
}
