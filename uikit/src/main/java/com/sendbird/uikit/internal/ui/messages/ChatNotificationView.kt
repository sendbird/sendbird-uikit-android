package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.databinding.SbViewChatNotificationComponentBinding
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.extensions.addRipple
import com.sendbird.uikit.internal.extensions.loadCircle
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.extensions.toStringMap
import com.sendbird.uikit.internal.model.notifications.NotificationConfig
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator
import com.sendbird.uikit.internal.singleton.MessageTemplateParser
import com.sendbird.uikit.internal.singleton.NotificationChannelManager
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.utils.DateUtils
import org.json.JSONObject

internal class ChatNotificationView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.sb_widget_chat_notification
) : BaseMessageView(context, attrs, defStyle) {
    override val binding: SbViewChatNotificationComponentBinding
    override val layout: View
        get() = binding.root

    var onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_ChatNotification, defStyle, 0)
        try {
            binding = SbViewChatNotificationComponentBinding.inflate(
                LayoutInflater.from(context),
                this,
                true
            )
            val messageBackground = a.getResourceId(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_background,
                R.color.background_100
            )
            val leftCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_category_text_appearance,
                R.style.SendbirdCaption1OnLight02
            )
            val rightCaptionAppearance = a.getResourceId(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_sent_at_text_appearance,
                R.style.SendbirdCaption4OnLight03
            )
            val bubbleRadius = a.getDimensionPixelSize(
                R.styleable.MessageView_ChatNotification_sb_chat_notification_radius,
                context.resources.getDimensionPixelSize(R.dimen.sb_size_8)
            )

            binding.contentPanel.setBackgroundResource(messageBackground)
            binding.tvCategory.setAppearance(context, leftCaptionAppearance)
            binding.tvSentAt.setAppearance(context, rightCaptionAppearance)
            binding.contentPanel.radius = bubbleRadius.toFloat()
        } finally {
            a.recycle()
        }
    }

    fun drawMessage(channel: BaseChannel, message: BaseMessage, config: NotificationConfig? = null) {
        binding.tvCategory.text = message.customType
        binding.tvSentAt.text = DateUtils.formatDateTime(context, message.createdAt)
        binding.ivProfileView.loadCircle(channel.coverUrl)

        // apply config
        config?.let {
            it.theme.notificationTheme.apply {
                val themeMode = config.themeMode
                category.apply {
                    binding.tvCategory.setTextColor(textColor.getColor(themeMode))
                    binding.tvCategory.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                }
                sentAt.apply {
                    binding.tvSentAt.setTextColor(textColor.getColor(themeMode))
                    binding.tvSentAt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                }

                val theme = this
                binding.contentPanel.apply {
                    isClickable = true
                    isFocusable = true
                    setRadiusIntSize(theme.radius)
                    setBackgroundColor(theme.backgroundColor.getColor(themeMode))
                    addRipple(theme.pressedColor.getColor(themeMode))
                }
            }
        }

        binding.contentPanel.removeAllViews()
        binding.contentPanel.addView(
            makeTemplateView(
                message,
                config?.themeMode ?: NotificationThemeMode.Default,
                onNotificationTemplateActionHandler
            )
        )
    }

    @Throws(Throwable::class)
    private fun makeTemplateView(
        message: BaseMessage,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ): View {
        return try {
            val subData: String = message.extendedMessage[KeySet.sub_data]
                ?: throw RuntimeException("this message must have template key.")
            val json = JSONObject(subData)
            val templateKey = json.getString(KeySet.template_key)
            var templateVariables: Map<String, String> = mapOf()
            if (json.has(KeySet.template_variables)) {
                templateVariables = json.getJSONObject(KeySet.template_variables).toStringMap()
            }
            val template = NotificationChannelManager.getTemplate(templateKey, templateVariables, themeMode)
            template?.let { jsonTemplate ->
                val viewParams: Params = MessageTemplateParser.parse(jsonTemplate)
                TemplateViewGenerator.inflateViews(context, viewParams) { view, params ->
                    params.action?.register(view, onNotificationTemplateActionHandler, message)
                }
            } ?: throw RuntimeException("binding color variables or data variables are failed")
        } catch (e: Throwable) {
            Logger.w("${e.printStackTrace()}")
            MessageTemplateParser.createDefaultViewParam(
                message,
                context.getString(R.string.sb_text_notification_fallback_title),
                context.getString(R.string.sb_text_notification_fallback_description),
                themeMode
            ).run {
                TemplateViewGenerator.inflateViews(context, this) { view, params ->
                    params.action?.register(view, onNotificationTemplateActionHandler, message)
                }
            }
        }
    }
}
