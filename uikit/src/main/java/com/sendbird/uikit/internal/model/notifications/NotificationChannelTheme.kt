@file:UseSerializers(CSVColorIntAsStringSerializer::class)
package com.sendbird.uikit.internal.model.notifications

import com.sendbird.uikit.internal.model.serializer.CSVColorIntAsStringSerializer
import com.sendbird.uikit.internal.model.template_messages.KeySet
import com.sendbird.uikit.internal.singleton.NotificationParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
internal data class NotificationChannelSettings(
    @SerialName(KeySet.updated_at)
    val updatedAt: Long,
    @SerialName(KeySet.theme_mode)
    val themeMode: NotificationThemeMode,
    val themes: List<NotificationChannelTheme>
) {
    companion object {
        @JvmStatic
        fun fromJson(jsonStr: String): NotificationChannelSettings {
            return NotificationParser.fromJson(jsonStr)
        }
    }

    override fun toString(): String {
        return NotificationParser.toJsonString(this)
    }

    fun getThemeOrNull(): NotificationChannelTheme? {
        return themes.firstOrNull()
    }
}

@Serializable
internal data class NotificationChannelTheme(
    val key: String,
    @SerialName(KeySet.created_at)
    val createdAt: Long,
    @SerialName(KeySet.updated_at)
    val updatedAt: Long,
    @SerialName(KeySet.notification)
    val notificationTheme: NotificationTheme,
    @SerialName(KeySet.list)
    val listTheme: NotificationListTheme,
    @SerialName(KeySet.header)
    val headerTheme: NotificationHeaderTheme
)

@Serializable
internal data class NotificationTheme(
    val radius: Int = 0,
    val backgroundColor: CSVColor,
    val unreadIndicatorColor: CSVColor,
    val category: FontStyle,
    val sentAt: FontStyle,
    val pressedColor: CSVColor
)

@Serializable
internal data class NotificationListTheme(
    val backgroundColor: CSVColor,
    val tooltip: TooltipStyle,
    val timeline: TimelineStyle
)

@Serializable
internal data class NotificationHeaderTheme(
    val textSize: Int,
    val textColor: CSVColor,
    val buttonIconTintColor: CSVColor,
    val backgroundColor: CSVColor,
    val lineColor: CSVColor
)

@Serializable
internal data class FontStyle(
    val textSize: Int,
    val textColor: CSVColor
)

@Serializable
internal data class TooltipStyle(
    val backgroundColor: CSVColor,
    val textColor: CSVColor
)

@Serializable
internal data class TimelineStyle(
    val backgroundColor: CSVColor,
    val textColor: CSVColor
)
