package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.databinding.SbViewOpenChannelAdminMessageBinding
import com.sendbird.uikit.model.MessageListUIParams

internal class OpenChannelAdminMessageViewHolder internal constructor(
    val binding: SbViewOpenChannelAdminMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.openChannelAdminMessageView.messageUIConfig = messageUIConfig
        binding.openChannelAdminMessageView.drawMessage(message)
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
