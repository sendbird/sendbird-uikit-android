package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.databinding.SbViewOtherMultipleFilesMessageBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnItemLongClickListener
import com.sendbird.uikit.model.MessageListUIParams

internal class OtherMultipleFilesMessageViewHolder internal constructor(
    val binding: SbViewOtherMultipleFilesMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.otherMultipleFilesMessageView.messageUIConfig = messageUIConfig
        if (channel is GroupChannel) {
            binding.otherMultipleFilesMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.otherMultipleFilesMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList)
            setEmojiReactionClickListener(emojiReactionClickListener)
            setEmojiReactionLongClickListener(emojiReactionLongClickListener)
            setMoreButtonClickListener(moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherMultipleFilesMessageView.binding.rvMultipleFilesMessage,
            ClickableViewIdentifier.Profile.name to binding.otherMultipleFilesMessageView.binding.ivProfileView,
            ClickableViewIdentifier.QuoteReply.name to binding.otherMultipleFilesMessageView.binding.quoteReplyPanel,
            ClickableViewIdentifier.ThreadInfo.name to binding.otherMultipleFilesMessageView.binding.threadInfo
        )
    }
}
