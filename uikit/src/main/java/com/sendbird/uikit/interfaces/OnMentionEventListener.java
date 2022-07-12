package com.sendbird.uikit.interfaces;

import androidx.annotation.Nullable;

/**
 * Interface to be invoked when the mentioned text is detected.
 *
 * @since 3.0.0
 */
public interface OnMentionEventListener {

    /**
     * Delivery detected text using a token.
     *
     * @param text the text detected by token.
     * @since 3.0.0
     */
    void onMentionedTextDetected(@Nullable CharSequence text);
}
