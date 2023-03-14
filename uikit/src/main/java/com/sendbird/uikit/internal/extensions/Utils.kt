package com.sendbird.uikit.internal.extensions

import android.os.Handler
import android.os.Looper

private val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }

internal fun <T> T?.runOnUiThread(block: (T) -> Unit) {
    if (this != null) {
        uiThreadHandler.post { block(this) }
    }
}
