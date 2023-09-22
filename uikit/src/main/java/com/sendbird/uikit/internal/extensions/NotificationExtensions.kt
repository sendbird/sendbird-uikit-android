package com.sendbird.uikit.internal.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.sendbird.android.params.MessageListParams
import com.sendbird.uikit.internal.model.notifications.NotificationConfig
import com.sendbird.uikit.internal.singleton.NotificationChannelManager.getGlobalNotificationChannelSettings
import com.sendbird.uikit.internal.ui.notifications.ChatNotificationChannelModule
import com.sendbird.uikit.internal.ui.notifications.FeedNotificationChannelModule
import com.sendbird.uikit.providers.ModuleProviders
import com.sendbird.uikit.providers.ViewModelProviders
import com.sendbird.uikit.vm.ChatNotificationChannelViewModel
import com.sendbird.uikit.vm.FeedNotificationChannelViewModel

internal fun Fragment.createFeedNotificationChannelModule(args: Bundle): FeedNotificationChannelModule {
    val config = getGlobalNotificationChannelSettings()?.let {
        NotificationConfig.from(it)
    }
    return ModuleProviders.feedNotificationChannel.provide(requireContext(), args, config)
}

internal fun Fragment.createChatNotificationChannelModule(args: Bundle): ChatNotificationChannelModule {
    val config = getGlobalNotificationChannelSettings()?.let {
        NotificationConfig.from(it)
    }
    return ModuleProviders.chatNotificationChannel.provide(requireContext(), args, config)
}

internal fun Fragment.createFeedNotificationChannelViewModel(
    channelUrl: String,
    params: MessageListParams?
): FeedNotificationChannelViewModel {
    return ViewModelProviders.feedNotificationChannel.provide(this, channelUrl, params)
}

internal fun Fragment.createChatNotificationChannelViewModel(
    channelUrl: String,
    params: MessageListParams?
): ChatNotificationChannelViewModel {
    return ViewModelProviders.chatNotificationChannel.provide(this, channelUrl, params)
}
