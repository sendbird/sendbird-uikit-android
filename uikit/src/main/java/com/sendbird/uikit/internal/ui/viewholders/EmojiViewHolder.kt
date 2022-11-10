package com.sendbird.uikit.internal.ui.viewholders

import com.sendbird.android.message.Emoji
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.databinding.SbViewEmojiBinding

internal class EmojiViewHolder(
    private val binding: SbViewEmojiBinding
) : BaseViewHolder<Emoji>(binding.root) {
    override fun bind(item: Emoji) {
        binding.emojiView.drawEmoji(item)
    }
}
