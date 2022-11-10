package com.sendbird.uikit.consts;

/**
 * Represents how to display replies in message list.
 *
 * @since 2.2.0
 */
public enum ReplyType {
    /**
     * Do not display replies in the message list.
     *
     * @since 2.2.0
     */
    NONE,
    /**
     * Displays replies linearly in the message list.
     *
     * @since 2.2.0
     */
    QUOTE_REPLY,
    /**
     * Displays replies to a parent message on a separate screen.
     *
     * @since 3.3.0
     */
    THREAD
}
