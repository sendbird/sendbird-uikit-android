package com.sendbird.uikit.interfaces;

import android.view.View;

/**
 * Interface definition for a callback to be invoked when a item is clicked.
 *
 * @since 1.2.0
 */
public interface OnMenuItemClickListener<T, R> {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param menu The menu item that was clicked.
     * @param data The data that was clicked.
     * @return true if the callback consumed the menu item click, false otherwise.
     */
    boolean onMenuItemClicked(View view, T menu, R data);
}
