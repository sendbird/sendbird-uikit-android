package com.sendbird.uikit.internal.wrappers

import com.sendbird.android.handler.ConnectHandler
import com.sendbird.uikit.SendbirdUIKit

internal class SendbirdUIKitImpl : SendbirdUIKitWrapper {
    override fun connect(handler: ConnectHandler?) {
        SendbirdUIKit.connect(handler)
    }

    override fun runOnUIThread(runnable: Runnable) {
        SendbirdUIKit.runOnUIThread(runnable)
    }
}
