package com.sendbird.uikit.interfaces;

import android.view.View;

import com.sendbird.uikit.consts.ClickableViewIdentifier;

/**
 * Interface definition for a callback to be invoked when a item is clicked.
 */
public interface OnIdentifiableItemClickListener<T> {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param identifier The clicked item identifier.
     * @param position The position that was clicked.
     * @param data The data that was clicked.
     *
     * @see ClickableViewIdentifier
     * @since 2.2.0
     */
    void onIdentifiableItemClick(View view, String identifier, int position, T data);
}
