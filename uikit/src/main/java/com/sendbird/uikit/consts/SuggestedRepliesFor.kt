package com.sendbird.uikit.consts

/**
 * Represents which messages to display suggested replies for.
 *
 * @since 3.17.0
 */
enum class SuggestedRepliesFor(val value: String) {
    /**
     * Show suggested replies for all messages.
     *
     * @since 3.17.0
     */
    ALL_MESSAGES(StringSet.all_messages),

    /**
     * Show suggested replies for last message only.
     *
     * @since 3.17.0
     */
    LAST_MESSAGE_ONLY(StringSet.last_message_only);

    companion object {
        /**
         * Convert to SuggestedRepliesFor that matches the given value.
         *
         * @param value the text value of the SuggestedRepliesFor.
         * @return the [SuggestedRepliesFor]
         * @since 3.17.0
         */
        @JvmStatic
        fun from(value: String): SuggestedRepliesFor {
            return values().firstOrNull { it.value == value } ?: LAST_MESSAGE_ONLY
        }
    }
}
