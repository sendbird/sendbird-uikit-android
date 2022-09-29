package com.sendbird.uikit.internal.extensions

import com.sendbird.android.message.BaseMessage

fun BaseMessage.hasParentMessage() = parentMessageId != 0L
