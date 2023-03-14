package com.sendbird.uikit.internal.singleton

import android.graphics.Color
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.internal.model.notifications.NotificationThemeMode
import com.sendbird.uikit.internal.model.template_messages.Body
import com.sendbird.uikit.internal.model.template_messages.BoxViewParams
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.model.template_messages.Margin
import com.sendbird.uikit.internal.model.template_messages.Orientation
import com.sendbird.uikit.internal.model.template_messages.Padding
import com.sendbird.uikit.internal.model.template_messages.Params
import com.sendbird.uikit.internal.model.template_messages.TextStyle
import com.sendbird.uikit.internal.model.template_messages.TextViewParams
import com.sendbird.uikit.internal.model.template_messages.ViewStyle
import com.sendbird.uikit.internal.model.template_messages.ViewType
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
    fun parse(jsonTemplate: String): Params {
        return when (val version = JSONObject(jsonTemplate).getInt(KeySet.version)) {
            1 -> parseParams(jsonTemplate)
            else -> throw RuntimeException("unsupported version. current version = $version")
        }
    }

    @JvmStatic
    fun createDefaultViewParam(
        message: BaseMessage,
        defaultFallbackTitle: String,
        defaultFallbackDescription: String,
        themeMode: NotificationThemeMode
    ): Params {
        val hasFallbackMessage = message.message.isNotEmpty()
        val textList = mutableListOf(
            TextViewParams(
                type = ViewType.Text,
                textStyle = TextStyle(
                    size = 14,
                    color = getTitleColor(themeMode)
                ),
                text = message.message.takeIf { it.isNotEmpty() } ?: defaultFallbackTitle
            )
        )

        if (!hasFallbackMessage) {
            textList.add(
                TextViewParams(
                    type = ViewType.Text,
                    textStyle = TextStyle(
                        size = 14,
                        color = getDescTextColor(themeMode)
                    ),
                    viewStyle = ViewStyle(
                        margin = Margin(
                            top = 10
                        )
                    ),
                    text = defaultFallbackDescription
                )
            )
        }
        return Params(
            version = 1,
            body = Body(
                items = listOf(
                    BoxViewParams(
                        type = ViewType.Box,
                        orientation = Orientation.Column,
                        viewStyle = ViewStyle(
                            backgroundColor = getBackgroundColor(themeMode),
                            padding = Padding(
                                12, 12, 12, 12
                            ),
                            radius = 8
                        ),
                        items = textList
                    ),
                )
            )
        )
    }

    private fun getBackgroundColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#EEEEEE"
            NotificationThemeMode.Dark -> "#2C2C2C"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#EEEEEE" else "#2C2C2C"
        }
        return Color.parseColor(color)
    }

    private fun getTitleColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#E0000000"
            NotificationThemeMode.Dark -> "#E0FFFFFF"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#E0000000" else "#E0FFFFFF"
        }
        return Color.parseColor(color)
    }

    private fun getDescTextColor(themeMode: NotificationThemeMode): Int {
        val color = when (themeMode) {
            NotificationThemeMode.Light -> "#70000000"
            NotificationThemeMode.Dark -> "#70FFFFFF"
            NotificationThemeMode.Default -> if (SendbirdUIKit.getDefaultThemeMode() == SendbirdUIKit.ThemeMode.Light) "#70000000" else "#70FFFFFF"
        }
        return Color.parseColor(color)
    }
}
