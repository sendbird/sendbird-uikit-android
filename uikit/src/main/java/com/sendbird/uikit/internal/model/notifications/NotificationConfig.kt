package com.sendbird.uikit.internal.model.notifications

import com.sendbird.uikit.interfaces.OnNotificationTemplateActionHandler

internal class NotificationConfig(
    val themeMode: NotificationThemeMode,
    val theme: NotificationChannelTheme
) {
    var onMessageTemplateActionHandler: OnNotificationTemplateActionHandler? = null
    companion object {
        @JvmStatic
        fun from(notificationChannelSettings: NotificationChannelSettings?): NotificationConfig? {
            return notificationChannelSettings?.let {
                NotificationConfig(
                    notificationChannelSettings.themeMode,
                    notificationChannelSettings.themes.first().copy()
                )
            }
        }
    }
}

