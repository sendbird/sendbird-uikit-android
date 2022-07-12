package com.sendbird.uikit.customsample.utils;

import androidx.annotation.NonNull;

import com.sendbird.android.handler.PushRequestCompleteHandler;
import com.sendbird.android.push.SendbirdPushHandler;
import com.sendbird.android.push.SendbirdPushHelper;

/**
 * This provides methods to manage push handler.
 */
public class PushUtils {

    public static void registerPushHandler(@NonNull SendbirdPushHandler handler) {
        SendbirdPushHelper.registerPushHandler(handler);
    }

    public static void unregisterPushHandler(@NonNull PushRequestCompleteHandler listener) {
        SendbirdPushHelper.unregisterPushHandler(listener);
    }
}
