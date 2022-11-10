package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.databinding.SbViewOpenChannelUserMessageBinding
import com.sendbird.uikit.model.MessageListUIParams

internal class OpenChannelUserMessageViewHolder internal constructor(
    val binding: SbViewOpenChannelUserMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.otherMessageView.messageUIConfig = messageUIConfig
        if (channel is OpenChannel) {
            binding.otherMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherMessageView.binding.contentPanel,
            ClickableViewIdentifier.Profile.name to binding.otherMessageView.binding.ivProfileView
        )
    }
}
