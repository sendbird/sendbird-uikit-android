package com.sendbird.uikit.internal.ui.viewholders

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.databinding.SbViewTimeLineMessageBinding
import com.sendbird.uikit.internal.model.notifications.NotificationConfig

internal class NotificationTimelineViewHolder internal constructor(
    val binding: SbViewTimeLineMessageBinding,
) : NotificationViewHolder(binding.root) {

    override fun bind(channel: GroupChannel, message: BaseMessage, config: NotificationConfig?) {
        binding.timelineMessageView.drawTimeline(message, config)
    }
}
