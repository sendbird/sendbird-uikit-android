package com.sendbird.uikit.internal.extensions

import com.sendbird.android.annotation.AIChatBotExperimental
import com.sendbird.android.channel.TemplateMessageData
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.internal.singleton.MessageTemplateManager
import com.sendbird.uikit.internal.singleton.MessageTemplateParser

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
    val template = MessageTemplateManager.getTemplate(key)
    if (template != null) {
        val syntax = template.getTemplateSyntax(
            templateMessageData.variables,
            templateMessageData.viewVariables
        )

        try {
            val params = MessageTemplateParser.parse(syntax)
            this.messageTemplateStatus = MessageTemplateStatus.CACHED
            this.messageTemplateParams = params
        } catch (e: Exception) {
            this.messageTemplateStatus = MessageTemplateStatus.FAILED_TO_PARSE
        }
    } else {
        this.messageTemplateStatus = MessageTemplateStatus.FAILED_TO_FETCH
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
internal var BaseMessage.messageTemplateParams: Params?
    get() = extras[StringSet.message_template_params] as? Params
    set(value) {
        if (value == null) {
            extras.remove(StringSet.message_template_params)
        } else {
            extras[StringSet.message_template_params] = value
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
