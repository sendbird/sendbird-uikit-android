package com.sendbird.uikit.samples.customization.channelsettings

import android.app.Activity
import com.sendbird.uikit.activities.ChannelSettingsActivity
import com.sendbird.uikit.consts.SingleMenuType
import com.sendbird.uikit.interfaces.providers.ChannelSettingsModuleProvider
import com.sendbird.uikit.modules.ChannelSettingsModule
import com.sendbird.uikit.modules.components.ChannelSettingsMenuComponent
import com.sendbird.uikit.providers.ModuleProviders
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.customization.GroupChannelRepository

fun showAppendNewCustomGroupChannelSettingsMenuSample(activity: Activity) {
    // You can customize the Group Channel settings menu using the following code.
    // The following code is an example of how to customize the Group Channel settings menu.
    // If you want to handle the CUSTOM menu click event, you should handle it yourself after creating a custom menu view.
    ModuleProviders.channelSettings = ChannelSettingsModuleProvider { context, _ ->
        ChannelSettingsModule(context).apply {
            val customMenuList = ChannelSettingsMenuComponent.defaultMenuSet.toMutableList().apply {
                add(ChannelSettingsMenuComponent.Menu.CUSTOM)
            }
            val component = ChannelSettingsMenuComponent().apply {
                // set the custom menu list.
                params.setMenuList(customMenuList) { context, _ -> // create custom menu view.
                    createMenuView(
                        context,
                        "Go to Chat",
                        null,
                        SingleMenuType.NONE,
                        R.drawable.icon_chat,
                        0
                    ).apply {
                        // set the click event listener here.
                        setOnClickListener {
                            println(">>>>>> Go to Chat")
                        }
                    }
                }
            }
            setChannelSettingsMenuComponent(component)
        }
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelSettingsActivity.newIntent(activity, channel.url))
    }
}

fun showCustomGroupChannelSettingsMenuSample(activity: Activity) {
    // You can customize the Group Channel settings menu using the following code.
    // It shows how to make custom menu items in the Group Channel settings menu.
    // If you want to handle the CUSTOM menu click event, you should handle it yourself after creating a custom menu view.
    ModuleProviders.channelSettings = ChannelSettingsModuleProvider { context, _ ->
        val module = ChannelSettingsModule(context).apply {
            val component = ChannelSettingsMenuComponent().apply {
                // set the custom menu list.
                params.setMenuList(
                    listOf(
                        ChannelSettingsMenuComponent.Menu.CUSTOM,
                        ChannelSettingsMenuComponent.Menu.MEMBERS,
                        ChannelSettingsMenuComponent.Menu.LEAVE_CHANNEL,
                    )
                ) { context, _ -> // create custom menu view.
                    createMenuView(
                        context,
                        "Go to Chat",
                        null,
                        SingleMenuType.NEXT,
                        R.drawable.icon_chat,
                        0
                    ).apply {
                        // set the click event listener here.
                        setOnClickListener {
                            println(">>>>>> Go to Chat")
                        }
                    }
                }
            }
            setChannelSettingsMenuComponent(component)
        }
        module
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelSettingsActivity.newIntent(activity, channel.url))
    }
}

fun showHidingChannelSettingsMenuSample(activity: Activity) {
    // It shows how to hide the default menu items in the Group Channel settings menu.
    ModuleProviders.channelSettings = ChannelSettingsModuleProvider { context, _ ->
        val module = ChannelSettingsModule(context).apply {
            val customMenuList = ChannelSettingsMenuComponent.defaultMenuSet.toMutableList().apply {
                remove(ChannelSettingsMenuComponent.Menu.LEAVE_CHANNEL)
            }
            val component = ChannelSettingsMenuComponent().apply {
                // hide LEAVE_CHANNEL menu.
                params.setMenuList(customMenuList, null)
            }
            setChannelSettingsMenuComponent(component)
        }
        module
    }

    GroupChannelRepository.getRandomChannel(activity) { channel ->
        activity.startActivity(ChannelSettingsActivity.newIntent(activity, channel.url))
    }
}
