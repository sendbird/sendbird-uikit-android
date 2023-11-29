package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.databinding.SbViewTypingIndicatorMessageBinding
import com.sendbird.uikit.model.MessageListUIParams
import com.sendbird.uikit.model.TypingIndicatorMessage

internal class TypingIndicatorViewHolder internal constructor(
    val binding: SbViewTypingIndicatorMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        if (message is TypingIndicatorMessage) {
            binding.typingIndicatorMessageView.updateTypingMembers(message.typingUsers)
        }
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
