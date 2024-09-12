package com.sendbird.uikit.internal.model.templates

import com.sendbird.android.channel.SimpleTemplateData
import com.sendbird.uikit.internal.model.notifications.CSVColor
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.serializer.JsonElementToStringSerializer
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.singleton.JsonParser
import com.sendbird.uikit.internal.singleton.MessageTemplateManager
import com.sendbird.uikit.log.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

// TODO : Bind with [NotificationTemplate] after api spec finalize
@Serializable
internal data class MessageTemplate(
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

    fun getTemplateSyntax(
        variables: Map<String, String>,
        viewVariables: Map<String, List<SimpleTemplateData>> = emptyMap()
    ): String {
        return _uiTemplate
            .replaceVariables(variables)
            .replaceViewVariables(viewVariables)
    }

    private fun String.replaceVariables(variables: Map<String, String>): String {
        val regex = "\\{([^{}]+)\\}".toRegex()
        return regex.replace(this) { matchResult ->
            val variable = matchResult.groups[1]?.value
            var converted = false

            // 1. lookup and convert color variables first
            var convertedResult = _colorVariables[variable]?.let {
                Logger.i("++ color variable key=$variable, value=$it")
                converted = true
                val csvColor = CSVColor(it)
                csvColor.getColorHexString(NotificationThemeMode.Default)
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

    /**
     * If there is problem while replacing view variables, it will return original string and it will be failed to parse to Params. It's intended.
     */
    private fun String.replaceViewVariables(viewVariables: Map<String, List<SimpleTemplateData>>): String {
        val regex = """\"\{@([^{}]+)\}\"""".toRegex() // find `"{@variable}"` pattern including `"`
        return regex.replace(this) { matchResult ->
            val variable = matchResult.groups[1]?.value ?: return@replace matchResult.value
            val variableDataList = viewVariables[variable] ?: return@replace matchResult.value
            val jsonArray = JSONArray()
            variableDataList.forEach { childTemplateData ->
                val template = MessageTemplateManager.getTemplate(childTemplateData.key) ?: return@replace matchResult.value
                jsonArray.put(JSONObject(template.getTemplateSyntax(childTemplateData.variables)))
            }

            jsonArray.toString()
        }
    }

    override fun toString(): String {
        return JsonParser.toJsonString(this)
    }

    companion object {
        @JvmStatic
        fun fromJson(value: String): MessageTemplate {
            return JsonParser.fromJson(value)
        }
    }
}
