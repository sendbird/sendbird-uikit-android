package com.sendbird.uikit.internal.extensions

import com.sendbird.android.annotation.AIChatBotExperimental
import com.sendbird.android.channel.TemplateMessageData
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.shadow.com.google.gson.JsonParser
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.internal.singleton.MessageTemplateManager
import com.sendbird.uikit.internal.singleton.MessageTemplateParser

internal const val MAX_CHILD_COUNT = 10

internal fun BaseMessage.isTemplateMessage(): Boolean {
    return this.templateMessageData != null
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

internal val BaseMessage.messageTemplateContainerType: MessageTemplateContainerType
    get() = try {
        val uiObj = this.extendedMessagePayload[StringSet.ui]
        val containerType = JsonParser.parseString(uiObj).asJsonObject.get(StringSet.container_type).asString
        MessageTemplateContainerType.create(containerType)
    } catch (_: Exception) {
        MessageTemplateContainerType.DEFAULT
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
    DEFAULT, WIDE, CAROUSEL;

    companion object {
        fun create(value: String?): MessageTemplateContainerType {
            return when (value) {
                "wide" -> WIDE
                else -> DEFAULT
            }
        }
    }
}

internal const val ERR_MESSAGE_TEMPLATE_NOT_APPLICABLE = "NOT_APPLICABLE"
internal const val ERR_MESSAGE_TEMPLATE_UNKNOWN = "UNKNOWN"
