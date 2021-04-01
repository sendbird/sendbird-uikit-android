package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public interface DialogProvider {
    void showWaitingDialog();
    void dismissWaitingDialog();

    void toastError(@StringRes int messageRes);
    void toastError(@NonNull String message);
    void toastSuccess(@NonNull int messageRes);
}
