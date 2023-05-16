package com.sendbird.uikit.internal.wrappers

import com.sendbird.android.handler.ConnectHandler

internal interface SendbirdUIKitWrapper {
    fun connect(handler: ConnectHandler?)
    fun runOnUIThread(runnable: Runnable)
}
