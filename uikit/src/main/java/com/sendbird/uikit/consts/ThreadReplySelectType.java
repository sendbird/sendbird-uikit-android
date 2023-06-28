package com.sendbird.uikit.consts;

import androidx.annotation.NonNull;

/**
 * Represents where to go when selecting a reply.
 * This type is only available for <code>ReplyType.THREAD</code> type.
 *
 * since 3.3.0
 */
public enum ThreadReplySelectType {
    /**
     * Moves to parent message.
     *
     * since 3.3.0
     */
    PARENT(StringSet.parent),
    /**
     * Moves to thread page.
     *
     * since 3.3.0
     */
    THREAD(StringSet.thread);

    private final String value;

    ThreadReplySelectType(@NonNull String value) {
        this.value = value;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    /**
     * Convert to ReplyType that matches the given value.
     *
     * @param value the text value of the ReplyType.
     * @return the {@link ReplyType}
     * since 3.6.0
     */
    @NonNull
    public static ThreadReplySelectType from(@NonNull String value) {
        for (ThreadReplySelectType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return THREAD;
    }
}
