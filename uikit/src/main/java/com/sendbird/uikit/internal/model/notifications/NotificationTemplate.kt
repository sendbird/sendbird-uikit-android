package com.sendbird.uikit.internal.model.notifications

import com.sendbird.uikit.internal.model.serializer.JsonElementToStringSerializer
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.singleton.JsonParser
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
            return JsonParser.fromJson(value)
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
    @SerialName(KeySet.data_template)
    @Serializable(with = JsonElementToStringSerializer::class)
    private val _dataTemplate: String,
    @SerialName(KeySet.color_variables)
    private val _colorVariables: Map<String, String>
) {

    companion object {
        @JvmStatic
        fun fromJson(value: String): NotificationTemplate {
            return JsonParser.fromJson(value)
        }
    }

    /**
     * If the data template is empty, it returns the UI template.
     */
    private fun validTemplateSyntax(): String {
        if (_uiTemplate.length > 2) {
            return _uiTemplate
        }
        return _dataTemplate
    }

    /**
     * If the data template is not empty, it returns true.
     */
    val isDataTemplate: Boolean
        get() = _dataTemplate.length > 2

    fun getTemplateSyntax(variables: Map<String, String>, themeMode: NotificationThemeMode): String {
        val regex = "\\{([^{}]+)\\}".toRegex()
        val template = validTemplateSyntax()
        return regex.replace(template) { matchResult ->
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
        return JsonParser.toJsonString(this)
    }
}
