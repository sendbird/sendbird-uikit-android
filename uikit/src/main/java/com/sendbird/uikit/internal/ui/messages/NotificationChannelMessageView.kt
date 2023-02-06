package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.databinding.SbViewMessageNotificationChannelComponentBinding
import com.sendbird.uikit.interfaces.OnMessageTemplateActionHandler
import com.sendbird.uikit.internal.MessageTemplateParser
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.utils.DateUtils
import com.sendbird.uikit.utils.DrawableUtils
import com.sendbird.uikit.utils.ViewUtils

internal class NotificationChannelMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_notification_channel_message
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewMessageNotificationChannelComponentBinding
    override val layout: View
        get() = binding.root

    var onMessageTemplateActionHandler: OnMessageTemplateActionHandler? = null

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_NotificationChannel, defStyle, 0)
        try {
            binding = SbViewMessageNotificationChannelComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_NotificationChannel_sb_notification_channel_message_background,
                R.color.background_100
            )
            val leftCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_NotificationChannel_sb_message_sender_name_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val rightCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_NotificationChannel_sb_message_time_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val bubbleRadius = a.getDimensionPixelSize(
                R.styleable.MessageView_NotificationChannel_sb_notification_channel_message_radius,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_8)
            )
            val newBadgeColor = a.getResourceId(
                R.styleable.MessageView_NotificationChannel_sb_notification_channel_new_badge_color,
                R.color.secondary_300
            )

            binding.contentPanel.setBackgroundResource(messageBackground)
            binding.tvLeftCaption.setAppearance(context, leftCaptionAppearance)
            binding.tvRightCaption.setAppearance(context, rightCaptionAppearance)
            binding.contentPanel.radius = bubbleRadius.toFloat()
            binding.ivNewBadge.background = DrawableUtils.createOvalIcon(context, newBadgeColor)
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(message: BaseMessage, shouldDisplayUserProfile: Boolean, lastSeen: Long) {
        if (shouldDisplayUserProfile) {
            ViewUtils.drawNotificationProfile(binding.ivProfileView, message)
        }
        binding.ivProfileView.visibility = if (shouldDisplayUserProfile) VISIBLE else GONE

        binding.tvLeftCaption.text = message.customType
        binding.tvRightCaption.text = DateUtils.formatDateTime(context, message.createdAt)
        binding.ivNewBadge.visibility =
            if (message.createdAt > lastSeen) View.VISIBLE else View.GONE

        binding.contentPanel.removeAllViews()
        val params = MessageTemplateParser.parseParams(
            message,
            context.getString(R.string.sb_text_notification_channel_error)
        )
        val layout = MessageTemplateView(context)
        layout.inflateViews(params.body.items, message, onMessageTemplateActionHandler)
        binding.contentPanel.addView(layout)
    }
}
