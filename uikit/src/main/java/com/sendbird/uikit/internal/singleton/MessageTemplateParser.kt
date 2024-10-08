package com.sendbird.uikit.internal.singleton

import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TemplateParamsCreator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.json.JSONObject

internal object MessageTemplateParser {
    private val json by lazy {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true

            // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#coercing-input-values
            // coerceInputValues = true

            // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#encoding-defaults
            // encodeDefaults = true
        }
    }

    @JvmStatic
    fun parseParams(el: JsonElement): Params {
        return json.decodeFromJsonElement(el)
    }

    @JvmStatic
    fun parseParams(jsonStr: String): Params {
        return json.decodeFromString(jsonStr)
    }

    @JvmStatic
    fun parseToMap(jsonStr: String): Map<String, String> {
        return json.decodeFromString(jsonStr)
    }

    @JvmStatic
    @Throws(Exception::class)
    fun parse(template: String): Params {
        return when (val version = JSONObject(template).getInt(KeySet.version)) {
            1, 2 -> parseParams(template)
            else -> throw RuntimeException("unsupported version. current version = $version")
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun parseDataTemplate(template: String): Params {
        return TemplateParamsCreator.createDataTemplateViewParams(template, NotificationThemeMode.Default)
    }
}
