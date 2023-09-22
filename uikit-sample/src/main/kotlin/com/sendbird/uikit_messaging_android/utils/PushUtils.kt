package com.sendbird.uikit_messaging_android.utils

import com.sendbird.android.handler.PushRequestCompleteHandler
import com.sendbird.android.push.SendbirdPushHandler
import com.sendbird.android.push.SendbirdPushHelper

/**
 * This provides methods to manage push handler.
 */
object PushUtils {
    fun registerPushHandler(handler: SendbirdPushHandler) {
        SendbirdPushHelper.registerPushHandler(handler)
    }

    fun unregisterPushHandler(listener: PushRequestCompleteHandler) {
        SendbirdPushHelper.unregisterPushHandler(listener)
    }
}
