package com.sendbird.uikit.interfaces;

import java.util.List;

/**
 * Interface definition for a callback to be invoked when a result of a list of user is loaded.
 */
public interface UserListResultHandler {
    /**
     * Called when a result of a list of user is loaded.
     *
     * @param userList The list of user that was loaded.
     * @param e The exception that was loaded.
     */
    void onResult(List<? extends UserInfo> userList, Exception e);
}
