package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.consts.MessageGroupType

internal abstract class OpenChannelMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    abstract fun drawMessage(channel: OpenChannel, message: BaseMessage, messageGroupType: MessageGroupType)
}
