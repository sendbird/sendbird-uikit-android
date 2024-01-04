package com.sendbird.uikit.internal.interfaces

/**
 * On visible item detect listener
 */
@JvmSuppressWildcards
internal fun interface OnImpressionDetectedListener<T> {
    fun onImpressionDetected(items: List<T>)
}
