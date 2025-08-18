package com.sendbird.uikit.model

/**
 * Data class representing the new line data for new line display.
 *
 * @property prevPosition The position of the previous new line in the list, or null if there is no previous new line.
 * @property currentPosition The position of the current new line in the list, or null if there is no current new line.
 * since 3.24.0
 */
data class NewLineData(
    val prevPosition: Int?,
    val currentPosition: Int?,
)
