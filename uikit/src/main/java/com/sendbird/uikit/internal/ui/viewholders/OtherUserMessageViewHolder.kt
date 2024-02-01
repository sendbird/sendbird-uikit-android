package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.user.User
import com.sendbird.uikit.activities.viewholder.GroupChannelMessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.databinding.SbViewOtherUserMessageBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnItemLongClickListener
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener
import com.sendbird.uikit.model.MessageListUIParams

internal class OtherUserMessageViewHolder internal constructor(
    val binding: SbViewOtherUserMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.otherMessageView.messageUIConfig = messageUIConfig
        if (channel is GroupChannel) {
            binding.otherMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.otherMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList)
            setEmojiReactionClickListener(emojiReactionClickListener)
            setEmojiReactionLongClickListener(emojiReactionLongClickListener)
            setMoreButtonClickListener(moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherMessageView.binding.contentPanel,
            ClickableViewIdentifier.Profile.name to binding.otherMessageView.binding.ivProfileView,
            ClickableViewIdentifier.QuoteReply.name to binding.otherMessageView.binding.quoteReplyPanel,
            ClickableViewIdentifier.ThreadInfo.name to binding.otherMessageView.binding.threadInfo
        )
    }

    fun setOnMentionClickListener(listener: OnItemClickListener<User>?) {
        binding.otherMessageView.mentionClickListener = listener
    }

    fun setOnFeedbackRatingClickListener(listener: OnFeedbackRatingClickListener?) {
        binding.otherMessageView.onFeedbackRatingClickListener = listener
    }
}
