package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.uikit.widgets.MessageInputView;

/**
 * Interface definition of a callback that is called when the input mode changes.
 */
public interface OnInputModeChangedListener {
    /**
     * Called when the input mode changes.
     *
     * @param before Input mode before change
     * @param current Input mode after change
     * @see MessageInputView.Mode
     */
    void onInputModeChanged(@NonNull MessageInputView.Mode before, @NonNull MessageInputView.Mode current);
}
