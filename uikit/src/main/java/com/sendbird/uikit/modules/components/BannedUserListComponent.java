package com.sendbird.uikit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.User;
import com.sendbird.uikit.activities.adapter.BannedUserListAdapter;

/**
 * This class creates and performs a view corresponding the banned user list area in Sendbird UIKit.
 *
 * @since 3.0.0
 */
public class BannedUserListComponent extends UserTypeListComponent<User> {
    @NonNull
    private BannedUserListAdapter adapter = new BannedUserListAdapter();

    /**
     * Constructor
     *
     * @since 3.0.0
     */
    public BannedUserListComponent() {
        super();
    }

    /**
     * Sets the banned user list adapter to provide child views on demand. The default is {@code new BannedUserListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends BannedUserListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }

    /**
     * Returns the banned user list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @NonNull
    @Override
    protected BannedUserListAdapter getAdapter() {
        return this.adapter;
    }
}
