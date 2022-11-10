package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.databinding.SbViewAdminMessageBinding
import com.sendbird.uikit.internal.ui.messages.AdminMessageView
import com.sendbird.uikit.model.MessageListUIParams

internal class AdminMessageViewHolder constructor(
    binding: SbViewAdminMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {
    private val adminMessageView: AdminMessageView

    init {
        adminMessageView = binding.adminMessageView
    }

    override fun bind(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
        adminMessageView.messageUIConfig = messageUIConfig
        adminMessageView.drawMessage(message)
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
