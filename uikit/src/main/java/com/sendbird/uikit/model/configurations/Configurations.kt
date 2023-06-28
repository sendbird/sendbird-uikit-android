package com.sendbird.uikit.model.configurations

import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.singleton.JsonParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Configurations internal constructor(
    @SerialName(KeySet.updated_at)
    internal val lastUpdatedAt: Long = 0L,
    @SerialName(KeySet.configuration)
    internal val uikitConfig: UIKitConfigurations = UIKitConfigurations()
) {
    companion object {
        @JvmStatic
        fun from(value: String): Configurations {
            return JsonParser.fromJson(value)
        }
    }

    fun toJson(): String {
        return JsonParser.toJsonString(this)
    }
}
