package com.sendbird.uikit.consts

/**
 * Represents the direction of suggested replies.
 *
 * @since 3.17.0
 */
enum class SuggestedRepliesDirection(val value: String) {
    /**
     * Displays suggested replies vertically.
     *
     * @since 3.17.0
     */
    VERTICAL(StringSet.vertical),

    /**
     * Displays suggested replies horizontally.
     *
     * @since 3.17.0
     */
    HORIZONTAL(StringSet.horizontal);

    companion object {
        /**
         * Convert to SuggestedRepliesDirection that matches the given value.
         *
         * @param value the text value of the SuggestedRepliesDirection.
         * @return the [SuggestedRepliesDirection]
         * @since 3.17.0
         */
        @JvmStatic
        fun from(value: String): SuggestedRepliesDirection {
            return values().firstOrNull { it.value == value } ?: VERTICAL
        }
    }
}
