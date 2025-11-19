package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.android.handler.ConnectionHandler;
import com.sendbird.uikit.interfaces.AuthenticateHandler;
import com.sendbird.uikit.internal.contracts.SendbirdUIKitContract;
import com.sendbird.uikit.internal.contracts.SendbirdUIKitImpl;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.MutableLiveDataEx;

import java.util.UUID;

/**
 * ViewModel preparing and managing data commonly used in UIKit's Activities or Fragments.
 *
 * since 3.0.0
 */
public abstract class BaseViewModel extends ViewModel {
    @NonNull
    protected final SendbirdUIKitContract sendbirdUIKit;
    @NonNull
    private final String connectionHandlerId = this.getClass().getSimpleName() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    private final MutableLiveData<Long> connectionDelayed = new MutableLiveData<>();
    private boolean connectionDelayedShown = false;

    protected BaseViewModel() {
        this(new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    BaseViewModel(@NonNull SendbirdUIKitContract sendbirdUIKit) {
        this.sendbirdUIKit = sendbirdUIKit;
    }

    void connect(@NonNull ConnectHandler handler) {
        Logger.dev(">> BaseViewModel::connect()");
        setConnectionDelayedHandler();
        sendbirdUIKit.connect(handler);
    }

    private void setConnectionDelayedHandler() {
        SendbirdChat.addConnectionHandler(connectionHandlerId, new ConnectionHandler() {
            @Override
            public void onConnected(@NonNull String userId) {
                Logger.dev(">> BaseViewModel::onConnected(%s)", userId);
                connectionDelayed.postValue(0L);
            }

            @Override
            public void onDisconnected(@NonNull String s) {}

            @Override
            public void onReconnectStarted() {}

            @Override
            public void onConnectionDelayed(long retryAfter) {
                Logger.dev(">> BaseViewModel::onConnectionDelayed() retryAfter: %d", retryAfter);
                postConnectionDelayed(retryAfter);
            }

            @Override
            public void onReconnectSucceeded() {
                Logger.dev(">> BaseViewModel::onReconnectSucceeded()");
                clearConnectionDelayed();
            }

            @Override
            public void onReconnectFailed() {}
        });
    }

    private void removeConnectionDelayedHandler() {
        SendbirdChat.removeConnectionHandler(connectionHandlerId);
    }

    private void postConnectionDelayed(long retryAfter) {
        connectionDelayedShown = true;
        connectionDelayed.postValue(retryAfter);
    }

    private void clearConnectionDelayed() {
        if (connectionDelayedShown) {
            connectionDelayed.postValue(0L);
            connectionDelayedShown = false;
        }
    }

    /**
     * Authenticates before using ViewModels data.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.0.0
     */
    abstract public void authenticate(@NonNull AuthenticateHandler handler);

    /**
     * Returns LiveData that can be observed if the connection is delayed.
     *
     * @return LiveData holding the `retryAter` value in seconds.
     * @since 3.25.0
     */
    @NonNull
    public LiveData<Long> onConnectionDelayed() {
        return connectionDelayed;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        removeConnectionDelayedHandler();
    }
}
