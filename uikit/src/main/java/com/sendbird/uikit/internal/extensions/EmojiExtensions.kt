package com.sendbird.uikit.internal.extensions

import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction

internal fun Collection<Emoji>.containsEmoji(emojiKey: String): Boolean {
    return this.any { it.key == emojiKey }
}

internal fun Collection<Reaction>.hasAllEmoji(emojiList: List<Emoji>): Boolean {
    return emojiList.all { emoji -> this.any { it.key == emoji.key } }
}
