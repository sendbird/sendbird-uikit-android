package com.sendbird.uikit.internal.ui.viewholders

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.databinding.SbViewChatNotificationBinding
import com.sendbird.uikit.internal.model.notifications.NotificationConfig

internal class ChatNotificationViewHolder internal constructor(
    val binding: SbViewChatNotificationBinding
) : NotificationViewHolder(binding.root) {

    override fun bind(channel: GroupChannel, message: BaseMessage, config: NotificationConfig?) {
        binding.chatNotification.drawMessage(channel, message, config)
    }
}
