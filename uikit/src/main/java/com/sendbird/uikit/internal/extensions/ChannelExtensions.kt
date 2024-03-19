package com.sendbird.uikit.internal.extensions

import com.sendbird.android.channel.GroupChannel
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.model.configurations.ChannelConfig

internal fun GroupChannel.shouldDisableInput(channelConfig: ChannelConfig): Boolean {
    return channelConfig.enableSuggestedReplies && this.lastMessage?.extendedMessagePayload?.get(StringSet.disable_chat_input) == true.toString()
}
