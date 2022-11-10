package com.sendbird.uikit.internal.ui.viewholders

import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.android.message.Reaction
import com.sendbird.uikit.R
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.internal.ui.reactions.EmojiReactionView
import com.sendbird.uikit.utils.DrawableUtils

internal class EmojiReactionMoreViewHolder(view: EmojiReactionView) : BaseViewHolder<Reaction>(view) {
    init {
        val a = view.context
            .theme
            .obtainStyledAttributes(
                null,
                R.styleable.EmojiReaction,
                R.attr.sb_widget_emoji_message,
                R.style.Widget_Sendbird_Emoji
            )
        try {
            val backgroundRes = a.getResourceId(
                R.styleable.EmojiReaction_sb_emoji_reaction_background,
                R.drawable.sb_emoji_reaction_background_light
            )
            val moreRes = a.getResourceId(
                R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src,
                R.drawable.icon_emoji_more
            )
            val moreResTint = a.getColorStateList(R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src_tint)
            view.setBackgroundResource(backgroundRes)
            moreResTint?.let { view.setImageDrawable(DrawableUtils.setTintList(view.context, moreRes, it)) }
                ?: view.setImageDrawable(AppCompatResources.getDrawable(view.context, moreRes))
            view.setCount(0)
        } finally {
            a.recycle()
        }
    }

    override fun bind(item: Reaction) {}
}
