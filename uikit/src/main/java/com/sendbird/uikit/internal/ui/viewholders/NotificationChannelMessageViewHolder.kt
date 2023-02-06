package com.sendbird.uikit.internal.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.databinding.SbViewMessageNotificationChannelBinding
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler

internal class NotificationChannelMessageViewHolder internal constructor(
    val binding: SbViewMessageNotificationChannelBinding,
    private val shouldDisplayUserProfile: Boolean
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(channel: BaseChannel, message: BaseMessage, lastSeenAt: Long) {
        if (channel is GroupChannel) {
            binding.messageTemplateView.drawMessage(message, shouldDisplayUserProfile, lastSeenAt)
            itemView.requestLayout()
        }
    }

    fun setOnMessageTemplateActionHandler(handler: OnMessageTemplateActionHandler?) {
        binding.messageTemplateView.onMessageTemplateActionHandler = handler
    }
}
