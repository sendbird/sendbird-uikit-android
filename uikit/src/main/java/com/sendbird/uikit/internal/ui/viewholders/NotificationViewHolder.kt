package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.model.notifications.NotificationConfig

internal abstract class NotificationViewHolder internal constructor(
    view: View
) : RecyclerView.ViewHolder(view) {

    abstract fun bind(channel: GroupChannel, message: BaseMessage, config: NotificationConfig?)
}
