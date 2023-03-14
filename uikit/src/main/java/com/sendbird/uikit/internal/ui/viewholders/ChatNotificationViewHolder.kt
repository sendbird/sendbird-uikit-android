package com.sendbird.uikit.internal.ui.viewholders

import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.databinding.SbViewChatNotificationBinding
import com.sendbird.uikit.internal.model.notifications.NotificationConfig

internal class ChatNotificationViewHolder internal constructor(
    val binding: SbViewChatNotificationBinding
) : NotificationViewHolder(binding.root) {

    override fun bind(channel: BaseChannel, message: BaseMessage, config: NotificationConfig?) {
        binding.chatNotification.onNotificationTemplateActionHandler = config?.onMessageTemplateActionHandler
        binding.chatNotification.drawMessage(channel, message, config)
    }
}
