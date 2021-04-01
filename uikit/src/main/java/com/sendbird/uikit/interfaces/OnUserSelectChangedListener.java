package com.sendbird.uikit.interfaces;

import java.util.List;

/**
 * Interface definition for a callback to be invoked when a user view holder is clicked.
 *
 * @since 1.2.0
 */
public interface OnUserSelectChangedListener {
    /**
     * Called when a user view holder has been clicked.
     *
     * @param selectedUsers The list of users who are selected.
     * @param isSelected Whether the view holder is checked or not.
     */
    void onUserSelectChanged(List<String> selectedUsers, boolean isSelected);
}
