package com.sendbird.uikit.adapter;

import android.content.Context;

import com.sendbird.android.handlers.InitResultHandler;
import com.sendbird.uikit.interfaces.UserInfo;

/**
 * Adapters provide a binding from a {@link com.sendbird.android.SendBird} set to a connection
 * within a {@link com.sendbird.uikit.SendBirdUIKit#init(SendBirdUIKitAdapter, Context)}.
 */
public interface SendBirdUIKitAdapter {
    /**
     * Provides the identifier of an app.
     *
     * @return the identifier of an app.
     */
    String getAppId();

    /**
     * Provides the access token to SendBird server.
     *
     * @return the access token.
     */
    String getAccessToken();

    /**
     * Provides the {@link UserInfo} to access SendBird server.
     *
     * @return the current user.
     */
    UserInfo getUserInfo();

    /**
     *
     * @return
     * @since 2.2.0
     */
    InitResultHandler getInitResultHandler();
}
