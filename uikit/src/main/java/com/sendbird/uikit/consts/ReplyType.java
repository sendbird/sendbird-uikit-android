package com.sendbird.uikit.consts;

import androidx.annotation.NonNull;

/**
 * Represents how to display replies in message list.
 *
 * since 2.2.0
 */
public enum ReplyType {
    /**
     * Do not display replies in the message list.
     *
     * since 2.2.0
     */
    NONE(StringSet.none),
    /**
     * Displays replies linearly in the message list.
     *
     * since 2.2.0
     */
    QUOTE_REPLY(StringSet.quote_reply),
    /**
     * Displays replies to a parent message on a separate screen.
     *
     * since 3.3.0
     */
    THREAD(StringSet.thread);

    private final String value;

    ReplyType(@NonNull String value) {
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
    public static ReplyType from(@NonNull String value) {
        for (ReplyType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return NONE;
    }
}
