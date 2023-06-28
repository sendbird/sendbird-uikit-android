package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.internal.SendbirdStatistics
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler
import com.sendbird.uikit.internal.extensions.intToDp
import com.sendbird.uikit.internal.extensions.toList
import com.sendbird.uikit.internal.extensions.toStringMap
import com.sendbird.uikit.internal.interfaces.GetTemplateResultHandler
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TemplateViewGenerator
import com.sendbird.uikit.internal.singleton.MessageTemplateParser
import com.sendbird.uikit.internal.singleton.NotificationChannelManager
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.utils.DrawableUtils
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
            override fun onResult(templateKey: String, jsonTemplate: String?, e: SendbirdException?) {
                Logger.d("++ get template has been succeed, matched=${parentView.tag == message.messageId}")
                if (parentView.tag == message.messageId) {
                    val layout = try {
                        e?.let { throw e }
                        jsonTemplate?.let {
                            val viewParams: Params = MessageTemplateParser.parse(jsonTemplate)
                            TemplateViewGenerator.inflateViews(context, viewParams) { view, params ->
                                params.action?.register(
                                    view,
                                    { v, action, message ->
                                        try {
                                            // if `tags` key doesn't exist, empty value has to delivery.(spec)
                                            val tags: List<String> =
                                                getExtendedSubData(message).optJSONArray(KeySet.tags)?.toList()
                                                    ?: listOf()
                                            val result = SendbirdStatistics.appendStat(
                                                KeySet.noti_stats,
                                                mapOf(
                                                    KeySet.action to KeySet.clicked,
                                                    KeySet.template_key to templateKey,
                                                    KeySet.channel_url to message.channelUrl,
                                                    KeySet.tags to tags,
                                                    KeySet.message_id to message.messageId,
                                                    KeySet.source to KeySet.notification,
                                                    KeySet.message_ts to message.createdAt,
                                                )
                                            )
                                            Logger.d("++ appendStat end, result=%s, tags=%s", result, tags)
                                        } catch (e: Throwable) {
                                            Logger.w(e)
                                        }
                                        onNotificationTemplateActionHandler?.onHandleAction(
                                            v, action, message
                                        )
                                    }, message
                                )
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

        var templateKey = ""
        try {
            parentView.removeAllViews()
            parentView.tag = message.messageId
            val json = getExtendedSubData(message)
            templateKey = json.getString(KeySet.template_key)
            var templateVariables: Map<String, String> = mapOf()
            if (json.has(KeySet.template_variables)) {
                templateVariables = json.getJSONObject(KeySet.template_variables).toStringMap()
            }
            if (!NotificationChannelManager.hasTemplate(templateKey)) {
                val layout = createLoadingView(!message.isFeedChannel, themeMode)
                parentView.addView(layout)
            }

            NotificationChannelManager.makeTemplate(
                templateKey, templateVariables, themeMode, handler
            )
        } catch (e: Throwable) {
            handler.onResult(templateKey, null, SendbirdException(e))
        }
    }

    @Throws(Exception::class)
    private fun getExtendedSubData(message: BaseMessage): JSONObject {
        val subData: String = message.extendedMessage[KeySet.sub_data]
            ?: throw IllegalArgumentException("this message must have sub data.")
        return JSONObject(subData)
    }

    internal fun createFallbackNotification(
        message: BaseMessage,
        themeMode: NotificationThemeMode,
        onNotificationTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    ): View {
        return TemplateViewGenerator.createDefaultViewParam(
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

    private fun createLoadingView(
        isChatNotification: Boolean,
        themeMode: NotificationThemeMode,
    ): View {
        return FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.intToDp(if (isChatNotification) 274 else 294),
            )
            addView(
                ProgressBar(context).apply {
                    val size = resources.intToDp(36)
                    layoutParams = LayoutParams(
                        size, size, Gravity.CENTER
                    )
                    val loading = DrawableUtils.setTintList(
                        context,
                        R.drawable.sb_progress,
                        ColorStateList.valueOf(TemplateViewGenerator.getSpinnerColor(themeMode))
                    )
                    this.indeterminateDrawable = loading
                }
            )
        }
    }
}
