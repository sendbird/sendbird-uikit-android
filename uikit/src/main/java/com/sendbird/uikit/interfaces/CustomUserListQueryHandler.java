package com.sendbird.uikit.interfaces;

/**
 * Interface definition for a callback to be invoked when a list of user is loaded.
 */
public interface CustomUserListQueryHandler {
    /**
     * Called when the user list is first loaded.
     *
     * @param handler The callback of result that was loaded.
     */
    void loadInitial(UserListResultHandler handler);

    /**
     * Called when a list of user has been loaded.
     *
     * @param handler The callback of result that was loaded.
     */
    void loadNext(UserListResultHandler handler);

    /**
     * Called whether a list of user is loaded.
     *
     * @return <code>true</code> if a list of user will be loaded, <code>false</code> otherwise.
     */
    boolean hasMore();
}
