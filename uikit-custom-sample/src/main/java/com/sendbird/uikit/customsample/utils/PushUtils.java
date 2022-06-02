package com.sendbird.uikit.customsample.utils;

import androidx.annotation.NonNull;

import com.sendbird.android.SendBirdPushHandler;
import com.sendbird.android.SendBirdPushHelper;

/**
 * This provides methods to manage push handler.
 */
public class PushUtils {

    public static void registerPushHandler(@NonNull SendBirdPushHandler handler) {
        SendBirdPushHelper.registerPushHandler(handler);
    }

    public static void unregisterPushHandler(@NonNull SendBirdPushHelper.OnPushRequestCompleteListener listener) {
        SendBirdPushHelper.unregisterPushHandler(listener);
    }
}
