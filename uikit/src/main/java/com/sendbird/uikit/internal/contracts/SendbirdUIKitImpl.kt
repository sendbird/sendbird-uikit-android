package com.sendbird.uikit.internal.contracts

import com.sendbird.android.handler.ConnectHandler
import com.sendbird.uikit.SendbirdUIKit

internal class SendbirdUIKitImpl : SendbirdUIKitContract {
    override fun connect(handler: ConnectHandler?) {
        SendbirdUIKit.connect(handler)
    }

    override fun runOnUIThread(runnable: Runnable) {
        SendbirdUIKit.runOnUIThread(runnable)
    }
}
