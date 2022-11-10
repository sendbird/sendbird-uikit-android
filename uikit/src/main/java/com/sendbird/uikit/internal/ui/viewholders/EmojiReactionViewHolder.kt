package com.sendbird.uikit.internal.ui.viewholders

import com.sendbird.android.message.Reaction
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.databinding.SbViewEmojiReactionBinding

internal class EmojiReactionViewHolder(
    private val binding: SbViewEmojiReactionBinding
) : BaseViewHolder<Reaction>(binding.root) {
    override fun bind(item: Reaction) {
        binding.emojiReactionView.drawReaction(item)
    }
}
