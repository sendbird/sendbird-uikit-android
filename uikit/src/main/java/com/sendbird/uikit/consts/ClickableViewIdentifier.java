package com.sendbird.uikit.consts;

/**
 * Identifier to determine which area was clicked when {@code MessageViewHolder} was clicked
 */
public enum ClickableViewIdentifier {
    /**
     * Main message area
     */
    Chat,
    /**
     * Sender's profile area
     */
    Profile,
    /**
     * Quoted message area
     */
    QuoteReply,
    /**
     * Thread information area
     *
     * since 3.3.0
     */
    ThreadInfo,
    /**
     * Parent message info menu area
     * This is only used in parent message info view
     *
     * since 3.3.0
     */
    ParentMessageMenu
}
