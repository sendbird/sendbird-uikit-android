package com.sendbird.uikit.internal.model

import com.sendbird.uikit.model.MessageDisplayData

internal data class MessageDisplayDataWrapper(
    val messageDisplayData: MessageDisplayData,
    val updatedAt: Long
)
