package com.sendbird.uikit.internal.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.FeedChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.databinding.SbViewFeedNotificationBinding
import com.sendbird.uikit.internal.model.notifications.NotificationConfig

internal class FeedNotificationViewHolder internal constructor(
    val binding: SbViewFeedNotificationBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        channel: FeedChannel,
        message: BaseMessage,
        lastSeenAt: Long,
        config: NotificationConfig?
    ) {
        binding.feedNotification.drawMessage(message, channel, lastSeenAt, config)
    }
}
