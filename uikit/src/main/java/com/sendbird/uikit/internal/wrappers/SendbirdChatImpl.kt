package com.sendbird.uikit.internal.wrappers

import com.sendbird.android.SendbirdChat
import com.sendbird.android.handler.BaseChannelHandler
import com.sendbird.android.handler.ConnectionHandler

internal class SendbirdChatImpl : SendbirdChatWrapper {
    override fun addChannelHandler(identifier: String, handler: BaseChannelHandler) {
        SendbirdChat.addChannelHandler(identifier, handler)
    }

    override fun addConnectionHandler(identifier: String, handler: ConnectionHandler) {
        SendbirdChat.addConnectionHandler(identifier, handler)
    }

    override fun removeChannelHandler(identifier: String): BaseChannelHandler? =
        SendbirdChat.removeChannelHandler(identifier)

    override fun removeConnectionHandler(identifier: String): ConnectionHandler? =
        SendbirdChat.removeConnectionHandler(identifier)
}
