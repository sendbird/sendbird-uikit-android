package com.sendbird.uikit.internal.extensions

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
