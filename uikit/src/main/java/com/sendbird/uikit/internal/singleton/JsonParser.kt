package com.sendbird.uikit.internal.singleton

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

internal object JsonParser {
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    @JvmStatic
    inline fun <reified T> fromJson(value: String): T {
        return json.decodeFromString(value)
    }

    @JvmStatic
    inline fun <reified T> toJsonString(value: T): String {
        return json.encodeToString(value)
    }

    @JvmStatic
    inline fun <reified T> fromJsonElement(element: JsonElement): T {
        return json.decodeFromJsonElement(element)
    }

    @JvmStatic
    internal fun toJsonElement(value: String): JsonElement {
        return json.parseToJsonElement(value)
    }
}
