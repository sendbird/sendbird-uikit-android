package com.sendbird.uikit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.consts.MessageGroupType

internal abstract class GroupChannelMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    abstract fun drawMessage(
        channel: GroupChannel,
        message: BaseMessage,
        messageGroupType: MessageGroupType
    )
}
