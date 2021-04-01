package com.sendbird.uikit.interfaces;

import com.sendbird.android.User;

/**
 * Interface definition for a callback to be invoked when a list of user is loaded.
 *
 * @since 1.2.0
 */
public interface CustomMemberListQueryHandler<T extends User> {

    /**
     * Called when the user list is first loaded.
     *
     * @param handler The callback of result that was loaded.
     */
    void loadInitial(OnListResultHandler<T> handler);
    /**
     * Called when a list of user has been loaded.
     *
     * @param handler The callback of result that was loaded.
     */
    void load(OnListResultHandler<T> handler);

    /**
     * Called whether a list of user is loaded.
     *
     * @return <code>true</code> if a list of user will be loaded, <code>false</code> otherwise.
     */
    boolean hasMore();
}
