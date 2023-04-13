package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.extensions.toStringMap
import com.sendbird.uikit.internal.interfaces.GetTemplateResultHandler
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator
import com.sendbird.uikit.internal.singleton.MessageTemplateParser
import com.sendbird.uikit.internal.singleton.NotificationChannelManager
import com.sendbird.uikit.log.Logger
import org.json.JSONObject

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
        val handler = object : GetTemplateResultHandler {
            override fun onResult(jsonTemplate: String?, e: SendbirdException?) {
                Logger.d("++ get template has been succeed, matched=${parentView.tag == message.messageId}")
                if (parentView.tag == message.messageId) {
                    val layout = try {
                        e?.let { throw e }
                        jsonTemplate?.let {
                            val viewParams: Params = MessageTemplateParser.parse(jsonTemplate)
                            TemplateViewGenerator.inflateViews(context, viewParams) { view, params ->
                                params.action?.register(view, onNotificationTemplateActionHandler, message)
                            }
                        }
                    } catch (e: Throwable) {
                        Logger.w("${e.printStackTrace()}")
                        createFallbackNotification(message, themeMode, onNotificationTemplateActionHandler)
                    }
                    parentView.removeAllViews()
                    parentView.addView(layout)
                }
            }
        }
        try {
            parentView.removeAllViews()
            parentView.tag = message.messageId
            val subData: String = message.extendedMessage[KeySet.sub_data]
                ?: throw IllegalArgumentException("this message must have template key.")
            val json = JSONObject(subData)
            val templateKey = json.getString(KeySet.template_key)
            var templateVariables: Map<String, String> = mapOf()
            if (json.has(KeySet.template_variables)) {
                templateVariables = json.getJSONObject(KeySet.template_variables).toStringMap()
            }
            if (!NotificationChannelManager.hasTemplate(templateKey)) {
                val layout = createFallbackNotification(message, themeMode, onNotificationTemplateActionHandler)
                parentView.addView(layout)
            }
            NotificationChannelManager.makeTemplate(
                templateKey,
                templateVariables,
                themeMode,
                handler
            )
        } catch (e: Throwable) {
            handler.onResult(null, SendbirdException(e))
        }
    }

    internal fun createFallbackNotification(
        message: BaseMessage,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ): View {
        return MessageTemplateParser.createDefaultViewParam(
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
