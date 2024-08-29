package com.sendbird.uikit.interfaces

import android.view.View
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction

internal fun interface EmojiReactionHandler {
    fun setEmojiReaction(
        reactionList: List<Reaction>,
        totalEmojiList: List<Emoji>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    )
}
