package com.sendbird.uikit.interfaces;

import android.view.View;

import com.sendbird.android.Emoji;

/**
 * Interface definition for a callback to be invoked when a emoji is clicked.
 */
public interface OnEmojiClickListener {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param position The position that was clicked.
     * @param emoji The emoji that was clicked.
     * @param viewType The viewType that was clicked.
     */
    void onEmojiClick(View view, int position, Emoji emoji, int viewType);
}
