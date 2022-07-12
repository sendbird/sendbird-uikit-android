package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.log.Logger;

/**
 * ViewModel preparing and managing data commonly used in UIKit's Activities or Fragments.
 *
 * @since 3.0.0
 */
public abstract class BaseViewModel extends ViewModel {
    void connect(@NonNull ConnectHandler handler) {
        Logger.dev(">> BaseViewModel::connect()");
        SendbirdUIKit.connect(handler);
    }

    /**
     * Authenticates before using ViewModels data.
     *
     * @param handler Callback notifying the result of authentication
     * @since 3.0.0
     */
    abstract public void authenticate(@NonNull AuthenticateHandler handler);
}
