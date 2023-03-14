package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.message.BaseMessage;

import java.util.List;

/**
 * Class that holds message data in a channel.
 *
 * @since 3.5.0
 */
public class MessageData {
    final List<BaseMessage> messages;
    final String traceName;

    public MessageData(@Nullable String traceName, @NonNull List<BaseMessage> messages) {
        this.traceName = traceName;
        this.messages = messages;
    }

    /**
     * Returns a list of messages for the current channel.
     *
     * @return A list of the latest messages on the current channel
     * @since 3.5.0
     */
    @NonNull
    public List<BaseMessage> getMessages() {
        return messages;
    }

    /**
     * Returns data indicating how the message list was updated.
     *
     * @return The String that traces the path of the message list
     * @since 3.5.0
     */
    @Nullable
    public String getTraceName() {
        return traceName;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "messages=" + messages +
                ", traceName='" + traceName + '\'' +
                '}';
    }
}
