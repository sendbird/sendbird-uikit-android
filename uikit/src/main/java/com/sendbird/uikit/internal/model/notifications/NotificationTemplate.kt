package com.sendbird.uikit.internal.model.notifications

import com.sendbird.uikit.internal.model.serializer.JsonElementToStringSerializer
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.singleton.NotificationParser
import com.sendbird.uikit.log.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationTemplateList constructor(
    val templates: List<NotificationTemplate>
) {
    companion object {
        @JvmStatic
        fun fromJson(value: String): NotificationTemplateList {
            return NotificationParser.fromJson(value)
        }
    }
}

@Serializable
internal data class NotificationTemplate constructor(
    @SerialName(KeySet.key)
    val templateKey: String,
    @SerialName(KeySet.created_at)
    val createdAt: Long,
    @SerialName(KeySet.updated_at)
    val updatedAt: Long,
    val name: String? = null,
    @SerialName(KeySet.ui_template)
    @Serializable(with = JsonElementToStringSerializer::class)
    private val _uiTemplate: String,
    @SerialName(KeySet.color_variables)
    private val _colorVariables: Map<String, String>
) {

    companion object {
        @JvmStatic
        fun fromJson(value: String): NotificationTemplate {
            return NotificationParser.fromJson(value)
        }
    }

    fun getTemplateSyntax(variables: Map<String, String>, themeMode: NotificationThemeMode): String {
        val regex = "\\{([^{}]+)\\}".toRegex()
        return regex.replace(_uiTemplate) { matchResult ->
            val variable = matchResult.groups[1]?.value
            var converted = false

            // 1. lookup and convert color variables first
            var convertedResult = _colorVariables[variable]?.let {
                Logger.i("++ color variable key=$variable, value=$it")
                converted = true
                val csvColor = CSVColor(it)
                csvColor.getColorHexString(themeMode)
            } ?: matchResult.value

            // 2. If color variables didn't convert, convert data variables then.
            if (!converted && variables.isNotEmpty()) {
                convertedResult = variables[variable]?.let {
                    Logger.i("++ data variable key=$variable, value=$it")
                    it
                } ?: convertedResult
            }
            convertedResult
        }
    }

    override fun toString(): String {
        return NotificationParser.toJsonString(this)
    }
}
