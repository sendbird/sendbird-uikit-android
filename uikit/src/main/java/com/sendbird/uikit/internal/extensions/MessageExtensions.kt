package com.sendbird.uikit.internal.extensions

import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.singleton.MessageDisplayDataManager
import com.sendbird.uikit.model.UserMessageDisplayData

internal fun BaseMessage.hasParentMessage() = parentMessageId != 0L

internal fun BaseMessage.getDisplayMessage(): String {
    return when (val data = MessageDisplayDataManager.getOrNull(this)) {
        is UserMessageDisplayData -> data.message ?: message
        else -> {
            message
        }
    }
}
