package com.sendbird.uikit.consts;

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
    PARENT,
    /**
     * Moves to thread page.
     *
     * since 3.3.0
     */
    THREAD
}
