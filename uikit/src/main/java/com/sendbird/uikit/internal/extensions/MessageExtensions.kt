package com.sendbird.uikit.internal.extensions

import com.sendbird.android.message.BaseMessage

internal fun BaseMessage.hasParentMessage() = parentMessageId != 0L
