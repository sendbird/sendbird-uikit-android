package com.sendbird.uikit.internal.model

import com.sendbird.android.channel.ChannelType
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.activities.viewholder.MessageType
import com.sendbird.uikit.internal.extensions.toStringMap
import com.sendbird.uikit.internal.model.template_messages.KeySet
import org.json.JSONObject

internal enum class ExtendedMessageType(val value: String) {
    Notification("0");

    companion object {
        @JvmStatic
        fun from(message: BaseMessage): MessageType? {
            val subType = message.extendedMessage[KeySet.sub_type]
            val subData = message.extendedMessage[KeySet.sub_data]
            return if (subData != null && subType != null) {
                val subDataMap = JSONObject(subData).toStringMap()
                val channelType = subDataMap[KeySet.channel_type]

                ExtendedMessageType.values().firstOrNull { it.value == subType }?.let {
                    when (it) {
                        Notification -> {
                            if (channelType == ChannelType.FEED.value) {
                                MessageType.VIEW_TYPE_FEED_NOTIFICATION
                            } else {
                                MessageType.VIEW_TYPE_CHAT_NOTIFICATION
                            }
                        }
                    }
                }
            } else null
        }
    }
}
