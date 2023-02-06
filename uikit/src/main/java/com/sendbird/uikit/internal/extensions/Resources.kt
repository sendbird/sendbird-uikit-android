package com.sendbird.uikit.internal.extensions

import android.content.res.Resources

// int value is a pure number value. For example it makes 10 and 10DP equal.
internal fun Resources.intToDp(value: Int): Int {
    return (value * displayMetrics.density + 0.5f).toInt()
}
