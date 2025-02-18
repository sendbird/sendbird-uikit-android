package com.sendbird.uikit.internal.model.notifications

import com.sendbird.message.template.model.MessageTemplate
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.singleton.JsonParser
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@Serializable
internal data class NotificationTemplateList(
    val templates: List<MessageTemplate>
) {
    companion object {
        @JvmStatic
        fun fromJson(value: String): NotificationTemplateList {
            val mutableTemplates = mutableListOf<MessageTemplate>()
            JsonParser.toJsonElement(value).jsonObject[KeySet.templates]?.jsonArray?.let { templateList ->
                for (element in templateList) {
                    try {
                        mutableTemplates.add(JsonParser.fromJsonElement(element))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return NotificationTemplateList(mutableTemplates.toList())
        }
    }
}
