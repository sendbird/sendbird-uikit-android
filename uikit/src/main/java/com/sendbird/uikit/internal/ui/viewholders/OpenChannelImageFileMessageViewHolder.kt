package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.databinding.SbViewOpenChannelFileImageMessageBinding
import com.sendbird.uikit.model.MessageListUIParams

internal class OpenChannelImageFileMessageViewHolder internal constructor(
    val binding: SbViewOpenChannelFileImageMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.openChannelImageFileMessageView.messageUIConfig = messageUIConfig
        if (channel is OpenChannel) {
            binding.openChannelImageFileMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.openChannelImageFileMessageView.binding.ivThumbnailOverlay,
            ClickableViewIdentifier.Profile.name to binding.openChannelImageFileMessageView.binding.ivProfileView
        )
    }
}
