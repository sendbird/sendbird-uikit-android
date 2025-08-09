package com.sendbird.uikit.samples.common.extensions

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

@Suppress("DEPRECATION")
internal fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

internal fun TextView.setTextColorResource(@ColorRes id: Int) {
    setTextColor(context.getColorResource(id))
}

internal fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

internal fun View.setInsetMargin(window: Window) {
    // For custom edge-to-edge system bar color
//    val edgeToEdgeConfig = EdgeToEdgeConfig(
//        statusBarColorLight = context.getColorResource(R.color.primary_300),
//        statusBarColorDark = context.getColorResource(R.color.primary_100)
//    )
//    SendbirdUIKit.setEdgeToEdgeConfig(edgeToEdgeConfig)

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        WindowInsetsCompat.CONSUMED
    }
}

