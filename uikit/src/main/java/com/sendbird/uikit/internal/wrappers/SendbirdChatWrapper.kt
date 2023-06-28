package com.sendbird.uikit.internal.wrappers

import com.sendbird.android.AppInfo
import com.sendbird.android.ConnectionState
import com.sendbird.android.handler.BaseChannelHandler
import com.sendbird.android.handler.CompletionHandler
import com.sendbird.android.handler.ConnectHandler
import com.sendbird.android.handler.ConnectionHandler
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.handler.UIKitConfigurationHandler
import com.sendbird.android.params.InitParams
import com.sendbird.android.params.UserUpdateParams

internal interface SendbirdChatWrapper {
    fun addChannelHandler(identifier: String, handler: BaseChannelHandler)
    fun addConnectionHandler(identifier: String, handler: ConnectionHandler)
    fun removeChannelHandler(identifier: String): BaseChannelHandler?
    fun removeConnectionHandler(identifier: String): ConnectionHandler?
    fun init(params: InitParams, handler: InitResultHandler)
    fun connect(userId: String, accessToken: String?, handler: ConnectHandler?)
    fun updateCurrentUserInfo(params: UserUpdateParams, handler: CompletionHandler?)
    fun addExtension(key: String, version: String)
    fun getAppInfo(): AppInfo?
    fun getConnectionState(): ConnectionState
    fun getUIKitConfiguration(handler: UIKitConfigurationHandler?)
}
