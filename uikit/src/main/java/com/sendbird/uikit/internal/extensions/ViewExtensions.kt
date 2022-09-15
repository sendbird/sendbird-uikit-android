package com.sendbird.uikit.internal.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sendbird.uikit.consts.StringSet

@Suppress("DEPRECATION")
fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

fun EditText.setCursorDrawable(context: Context, res: Int) {
    ContextCompat.getDrawable(context, res)?.let {
        setCursorDrawable(it)
    }
}

@SuppressLint("DiscouragedPrivateApi")
fun EditText.setCursorDrawable(cursor: Drawable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textCursorDrawable = cursor
    } else {
        try {
            val f = TextView::class.java.getDeclaredField(StringSet.mCursorDrawableRes)
            f.isAccessible = true
            f[this] = cursor
        } catch (t: Throwable) {
        }
    }
}
