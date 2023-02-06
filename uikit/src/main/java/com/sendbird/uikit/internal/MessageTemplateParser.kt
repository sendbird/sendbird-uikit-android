package com.sendbird.uikit.internal

import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.model.template_messages.Body
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.Padding
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TextStyle
import com.sendbird.uikit.internal.model.template_messages.TextViewParams
import com.sendbird.uikit.internal.model.template_messages.ViewStyle
import com.sendbird.uikit.internal.model.template_messages.ViewType
import com.sendbird.uikit.log.Logger
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
    fun parseParams(message: BaseMessage, defaultErrorMessage: String = ""): Params {
        return try {
            val subData: String = message.extendedMessage[KeySet.sub_data] ?: ""
            when (val version = JSONObject(subData).getInt(KeySet.version)) {
                1 -> parseParams(subData)
                else -> throw RuntimeException("unsupported version. current version = $version")
            }
        } catch (e: Throwable) {
            Logger.e("$e, data=${message.extendedMessage[KeySet.sub_data] ?: ""}")
            createDefaultViewParam(message, defaultErrorMessage)
        }
    }

    private fun createDefaultViewParam(message: BaseMessage, defaultErrorMessage: String): Params {
        return Params(
            version = 1,
            body = Body(
                items = listOf(
                    TextViewParams(
                        type = ViewType.Text,
                        viewStyle = ViewStyle(
                            padding = Padding(
                                12, 12, 12, 12
                            ),
                        ),
                        textStyle = TextStyle(
                            size = 14
                        ),
                        text = message.message.takeIf { it.isNotEmpty() } ?: defaultErrorMessage
                    )
                )
            )
        )
    }
}
