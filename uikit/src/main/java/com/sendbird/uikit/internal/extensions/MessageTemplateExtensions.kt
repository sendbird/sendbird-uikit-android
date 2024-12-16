package com.sendbird.uikit.internal.extensions

import com.sendbird.android.annotation.AIChatBotExperimental
import com.sendbird.android.channel.TemplateMessageData
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.BaseMessage
import com.sendbird.message.template.consts.MessageTemplateError
import com.sendbird.message.template.consts.TemplateTheme
import com.sendbird.message.template.model.TemplateParams
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.SendbirdUIKit.ThemeMode
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.internal.singleton.MessageTemplateManager

internal fun BaseMessage.isTemplateMessage(): Boolean {
    return this.templateMessageData != null
}

/**
 * Check if the message is a valid template message.
 * if the message is null or the type is not "default", it returns false.
 * @return `true` if the message is a valid template message, `false` otherwise.
 */
internal fun TemplateMessageData?.isValid(): Boolean {
    return this != null && MessageTemplateContainerType.from(type) != MessageTemplateContainerType.UNKNOWN
}

internal fun BaseMessage.saveParamsFromTemplate() {
    val templateMessageData = this.templateMessageData ?: return
    val key = templateMessageData.key

    try {
        val params = MessageTemplateManager.parseTemplate(key, templateMessageData.variables, templateMessageData.viewVariables)
        this.messageTemplateStatus = MessageTemplateStatus.CACHED
        this.messageTemplateParams = params
    } catch (e: SendbirdException) {
        when (e.code) {
            MessageTemplateError.ERROR_TEMPLATE_NOT_EXIST -> this.messageTemplateStatus = MessageTemplateStatus.FAILED_TO_FETCH
            MessageTemplateError.ERROR_TEMPLATE_PARSE_FAILED -> this.messageTemplateStatus = MessageTemplateStatus.FAILED_TO_PARSE
        }
    }
}

internal fun TemplateMessageData.childTemplateKeys(): List<String> {
    return viewVariables.values.flatten().map { it.key }.distinct()
}

internal val contentDisplayed: MutableMap<Long, Boolean> = mutableMapOf()
internal var BaseMessage.isContentDisplayed: Boolean
    get() = contentDisplayed[messageId] ?: false
    set(value) {
        contentDisplayed[messageId] = value
    }

@OptIn(AIChatBotExperimental::class)
internal var BaseMessage.messageTemplateStatus: MessageTemplateStatus?
    get() = extras[StringSet.message_template_status] as? MessageTemplateStatus
    set(value) {
        if (value == null) {
            extras.remove(StringSet.message_template_status)
        } else {
            extras[StringSet.message_template_status] = value
        }
    }

@OptIn(AIChatBotExperimental::class)
internal var BaseMessage.messageTemplateParams: TemplateParams?
    get() = extras[StringSet.message_template_params] as? TemplateParams
    set(value) {
        if (value == null) {
            extras.remove(StringSet.message_template_params)
        } else {
            extras[StringSet.message_template_params] = value
        }
    }

internal fun ThemeMode.toTemplateTheme(): TemplateTheme {
    return when (this) {
        ThemeMode.Light -> TemplateTheme.Light
        ThemeMode.Dark -> TemplateTheme.Dark
    }
}

internal fun NotificationThemeMode.toTemplateTheme(): TemplateTheme {
    return when (this) {
        NotificationThemeMode.Light -> TemplateTheme.Light
        NotificationThemeMode.Dark -> TemplateTheme.Dark
        NotificationThemeMode.Default -> SendbirdUIKit.getDefaultThemeMode().toTemplateTheme()
    }
}

internal enum class MessageTemplateContainerType {
    UNKNOWN, DEFAULT;

    companion object {
        fun from(value: String): MessageTemplateContainerType {
            return when (value.lowercase()) {
                StringSet.default -> DEFAULT
                else -> UNKNOWN
            }
        }
    }
}

internal const val ERR_MESSAGE_TEMPLATE_NOT_APPLICABLE = "NOT_APPLICABLE"
internal const val ERR_MESSAGE_TEMPLATE_UNKNOWN = "UNKNOWN"
