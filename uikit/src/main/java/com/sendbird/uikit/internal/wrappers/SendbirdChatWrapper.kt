package com.sendbird.uikit.internal.wrappers

import com.sendbird.android.handler.BaseChannelHandler
import com.sendbird.android.handler.ConnectionHandler

internal interface SendbirdChatWrapper {
    fun addChannelHandler(identifier: String, handler: BaseChannelHandler)
    fun addConnectionHandler(identifier: String, handler: ConnectionHandler)
    fun removeChannelHandler(identifier: String): BaseChannelHandler?
    fun removeConnectionHandler(identifier: String): ConnectionHandler?
}
