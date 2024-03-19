package com.sendbird.uikit.internal.interfaces

/**
 * On visible item detect listener
 */
@JvmSuppressWildcards
internal fun interface OnNotificationViewedDetectedListener<T> {
    fun onNotificationViewedDetected(items: List<T>)
}
