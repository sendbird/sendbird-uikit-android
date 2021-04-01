package com.sendbird.uikit.interfaces;

import android.view.View;

/**
 * Interface definition for a callback to be invoked when a item is clicked.
 */
public interface OnItemClickListener<T> {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param position The position that was clicked.
     * @param data The data that was clicked.
     */
    void onItemClick(View view, int position, T data);
}
