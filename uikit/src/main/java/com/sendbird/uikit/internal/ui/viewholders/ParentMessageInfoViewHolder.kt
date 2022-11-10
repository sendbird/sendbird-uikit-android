package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.databinding.SbViewParentMessageInfoHolderBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnItemLongClickListener
import com.sendbird.uikit.model.MessageListUIParams

internal class ParentMessageInfoViewHolder(val binding: SbViewParentMessageInfoHolderBinding) :
    GroupChannelMessageViewHolder(binding.root) {

    override fun bind(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
        binding.parentMessageInfoView.drawMessage((channel as GroupChannel), message, params)
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.parentMessageInfoView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList)
            setEmojiReactionClickListener(emojiReactionClickListener)
            setEmojiReactionLongClickListener(emojiReactionLongClickListener)
            setMoreButtonClickListener(moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.ParentMessageMenu.name to binding.parentMessageInfoView.binding.ivMoreIcon,
            ClickableViewIdentifier.Chat.name to binding.parentMessageInfoView.binding.contentPanel
        )
    }
}
