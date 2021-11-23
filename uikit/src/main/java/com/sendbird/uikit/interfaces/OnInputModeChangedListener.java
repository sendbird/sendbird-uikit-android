package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.uikit.widgets.MessageInputView;

public interface OnInputModeChangedListener {
    void onInputModeChanged(@NonNull MessageInputView.Mode before, @NonNull MessageInputView.Mode current);
}
