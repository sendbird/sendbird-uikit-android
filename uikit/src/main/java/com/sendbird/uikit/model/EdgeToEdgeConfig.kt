package com.sendbird.uikit.model

import androidx.annotation.ColorInt

/**
 * Describes a configuration for edge-to-edge UI.
 *
 * @since 3.23.0
 */
data class EdgeToEdgeConfig(
    /**
     * Returns the color of the status bar when UIKit theme is light.
     *
     * @since 3.23.0
     */
    @ColorInt
    val statusBarColorLight: Int? = null,

    /**
     * Returns the color of the status bar when UIKit theme is dark.
     *
     * @since 3.23.0
     */
    @ColorInt
    val statusBarColorDark: Int? = null
)
