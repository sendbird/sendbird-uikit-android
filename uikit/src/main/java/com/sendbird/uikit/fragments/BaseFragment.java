package com.sendbird.uikit.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.interfaces.DialogProvider;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.ReadyStatus;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.EventProvider;
import com.sendbird.uikit.widgets.WaitingDialog;

abstract class BaseFragment extends PermissionFragment implements DialogProvider {
    private final String CONNECTION_HANDLER_ID = getClass().getName() + System.currentTimeMillis();

    public abstract void onReady(User user, ReadyStatus status);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connect();
    }

    protected boolean isActive() {
        boolean isDeactivated = isRemoving() || isDetached() || getContext() == null;
        return !isDeactivated;
    }

    protected void connect() {
        Logger.dev(">> BaseFragment::connect()");
        SendBirdUIKit.connect((user, e) -> {
            Logger.dev("++ BaseFragment::connect e : " + e);
            ReadyStatus status;
            if (e != null) {
                if (SendBird.getCurrentUser() == null) {
                    status = ReadyStatus.ERROR;
                } else {
                    SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
                        @Override
                        public void onReconnectStarted() {}

                        @Override
                        public void onReconnectSucceeded() {
                            if (!isActive()) return;
                            SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
                            onReady(SendBird.getCurrentUser(), ReadyStatus.READY);
                        }

                        @Override
                        public void onReconnectFailed() {
                            if (!isActive()) return;
                            SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
                            onReady(null, ReadyStatus.ERROR);
                        }
                    });
                    return;
                }
            } else {
                status = ReadyStatus.READY;
            }

            if (!isActive()) return;
            onReady(SendBird.getCurrentUser(), status);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        EventProvider.getInstance().unRegister(getClass());
    }

    @Override
    public void showWaitingDialog() {
        WaitingDialog.show(getContext());
    }

    @Override
    public void dismissWaitingDialog() {
        WaitingDialog.dismiss();
    }

    @Override
    public void toastError(int messageRes) {
        ContextUtils.toastError(getContext(), messageRes);
    }

    @Override
    public void toastError(@NonNull String message) {
        ContextUtils.toastError(getContext(), message);
    }

    @Override
    public void toastSuccess(int messageRes) {
        ContextUtils.toastSuccess(getContext(), messageRes);
    }

    protected void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    protected boolean containsExtra(String key) {
        Bundle args = getArguments();
        return args != null && args.containsKey(key);
    }

    @Nullable
    protected String getStringExtra(String key) {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(key);
        }
        return null;
    }
}
