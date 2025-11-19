package com.sendbird.uikit.internal.extensions

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.StringSet
import java.util.Locale

internal fun String?.toDisplayText(default: String): String {
    return when {
        this == null -> default
        this.contains(StringSet.gif) -> StringSet.gif.uppercase(Locale.getDefault())
        this.startsWith(StringSet.image) -> StringSet.photo.upperFirstChar()
        this.startsWith(StringSet.video) -> StringSet.video.upperFirstChar()
        this.startsWith(StringSet.audio) -> StringSet.audio.upperFirstChar()
        else -> default
    }
}

internal fun String.upperFirstChar(): String {
    return this.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString() }
}

internal infix fun List<String>?.isEqualTo(other: List<String>?): Boolean {
    return this == other
}

internal fun Long.toEstimatedTimeString(context: Context): CharSequence {
    val minutes: Long = this / 60
    val remainingSeconds: Long = this % 60
    if (minutes == 0L && remainingSeconds == 0L) {
        return ""
    }
    val remainingTime = String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
    val formatted: String = String.format(
        context.getString(R.string.sb_text_connection_delayed_description),
        remainingTime
    )
    val ssb = SpannableStringBuilder(formatted)
    val start = formatted.indexOf(remainingTime)
    if (start >= 0) {
        val end = start + remainingTime.length
        ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
    }
    return ssb
}
