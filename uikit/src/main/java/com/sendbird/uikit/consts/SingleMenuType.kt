package com.sendbird.uikit.consts

/**
 * Single menu type
 *
 * @constructor Create empty Single menu type
 * @since 3.16.0
 */
enum class SingleMenuType(private val value: Int) {
    /**
     * A type that has an action button to redirect next page.
     */
    NEXT(0),

    /**
     * A type that has a switch button to toggle some action.
     */
    SWITCH(1),

    /**
     * A type that has no next action.
     */
    NONE(2);

    companion object {
        // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
        @JvmStatic
        fun from(value: Int): SingleMenuType = values().firstOrNull { it.value == value } ?: NONE
    }
}
