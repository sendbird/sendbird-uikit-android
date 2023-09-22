package com.sendbird.uikit.internal.extensions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper

private val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }

internal fun <T> T?.runOnUiThread(block: (T) -> Unit) {
    if (this != null) {
        uiThreadHandler.post { block(this) }
    }
}

// Returns Theme Context from defStyle
internal fun Context.toContextThemeWrapper(defStyle: Int): Context {
    val values = TypedValue()
    this.theme.resolveAttribute(defStyle, values, true)
    return ContextThemeWrapper(this, values.resourceId)
}
