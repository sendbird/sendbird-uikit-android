package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageViewHolder
import com.sendbird.uikit.consts.ClickableViewIdentifier
import com.sendbird.uikit.databinding.SbViewOtherTemplateMessageBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler
import com.sendbird.uikit.internal.interfaces.OnFeedbackRatingClickListener
import com.sendbird.uikit.internal.ui.messages.OtherTemplateMessageView
import com.sendbird.uikit.internal.utils.TemplateViewCachePool
import com.sendbird.uikit.model.MessageListUIParams

internal class OtherTemplateMessageViewHolder(
    val binding: SbViewOtherTemplateMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {
    lateinit var templateViewCachePool: TemplateViewCachePool

    private val messageView: OtherTemplateMessageView
        get() = binding.otherMessageView

    var onMessageTemplateActionHandler: OnMessageTemplateActionHandler? = null

    var onFeedbackRatingClickListener: OnFeedbackRatingClickListener? = null
    var onSuggestedRepliesClickListener: OnItemClickListener<String>? = null

    override fun bind(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
        binding.otherMessageView.messageUIConfig = messageUIConfig
        messageView.drawMessage(message, params, templateViewCachePool) { view, action, message ->
            onMessageTemplateActionHandler?.onHandleAction(view, action, message)
        }

        messageView.onFeedbackRatingClickListener = onFeedbackRatingClickListener
        messageView.onSuggestedRepliesClickListener = onSuggestedRepliesClickListener
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Profile.name to messageView.binding.ivProfileView,
        )
    }
}
