package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.NotificationData
import com.sendbird.android.internal.SendbirdStatistics
import com.sendbird.android.message.BaseMessage
import com.sendbird.message.template.ViewGenerator
import com.sendbird.uikit.R
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.extensions.isContentDisplayed
import com.sendbird.uikit.internal.extensions.messageTemplateStatus
import com.sendbird.uikit.internal.extensions.toTemplateTheme
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.TemplateParamsCreator
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.internal.singleton.NotificationChannelManager
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.Action

internal abstract class BaseNotificationView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {

    internal fun makeTemplateView(
        message: BaseMessage,
        parentView: ViewGroup,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ) {
        val notificationData: NotificationData? = message.notificationData
        val templateKey: String = notificationData?.templateKey ?: ""
        val templateVariables = notificationData?.templateVariables ?: mapOf()
        val layout: View = try {
            parentView.removeAllViews()

            if (templateKey.isEmpty()) {
                throw IllegalArgumentException("this message must have template key.")
            }
            message.isContentDisplayed = true
            when (message.messageTemplateStatus) {
                MessageTemplateStatus.CACHED -> {
                    val templateParams = NotificationChannelManager.parseTemplate(templateKey, themeMode, templateVariables)
                    ViewGenerator.inflateViews(
                        context,
                        themeMode.toTemplateTheme(),
                        templateParams,
                        onViewCreated = { view, params ->
                            params.action?.register(
                                view,
                                message
                            ) { v, action, message ->
                                sendNotificationStats(templateKey, message)
                                onNotificationTemplateActionHandler?.onHandleAction(
                                    v, Action.from(action), message
                                )
                            }
                        }
                    ).also {
                        message.isContentDisplayed = true
                    }
                }
                MessageTemplateStatus.LOADING -> TemplateParamsCreator.createNotificationLoadingView(context, !message.isFeedChannel, themeMode)
                null, MessageTemplateStatus.NOT_APPLICABLE,
                MessageTemplateStatus.FAILED_TO_PARSE,
                MessageTemplateStatus.FAILED_TO_FETCH -> throw IllegalArgumentException("fail to load this template message. id=${message.messageId}, status=${message.messageTemplateStatus}")
            }
        } catch (e: Throwable) {
            message.isContentDisplayed = false
            createFallbackNotification(message, themeMode, onNotificationTemplateActionHandler)
        }
        parentView.addView(layout)
    }

    private fun createFallbackNotification(
        message: BaseMessage,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ): View {
        return TemplateParamsCreator.createDefaultViewParam(
            message,
            context.getString(R.string.sb_text_notification_fallback_title),
            context.getString(R.string.sb_text_notification_fallback_description),
            themeMode
        ).run {
            ViewGenerator.inflateViews(
                context,
                themeMode.toTemplateTheme(),
                this,
                onViewCreated = { view, params ->
                    params.action?.register(view, message) { v, action, message ->
                        onNotificationTemplateActionHandler?.onHandleAction(
                            v, Action.from(action), message
                        )
                    }
                }
            )
        }
    }

    private fun sendNotificationStats(templateKey: String, message: BaseMessage) {
        try {
            // if `tags` key doesn't exist, empty value has to delivery.(spec)
            val tags: List<String> = message.notificationData?.tags ?: listOf()
            val result = SendbirdStatistics.appendStat(
                KeySet.noti_stats,
                mutableMapOf(
                    KeySet.action to KeySet.clicked,
                    KeySet.template_key to templateKey,
                    KeySet.channel_url to message.channelUrl,
                    KeySet.tags to tags,
                    KeySet.message_id to message.messageId,
                    KeySet.source to KeySet.notification,
                    KeySet.message_ts to message.createdAt,
                ).apply {
                    message.notificationEventDeadline?.let {
                        put(KeySet.notification_event_deadline, it)
                    }
                }.toMap()
            )
            Logger.d("++ appendStat end, result=%s, tags=%s", result, tags)
        } catch (e: Throwable) {
            Logger.w(e)
        }
    }
}
