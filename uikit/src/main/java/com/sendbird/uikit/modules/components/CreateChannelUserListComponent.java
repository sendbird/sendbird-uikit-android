package com.sendbird.uikit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.uikit.activities.adapter.CreateChannelUserListAdapter;
import com.sendbird.uikit.interfaces.UserInfo;

/**
 * This class creates and performs a view corresponding the user list area when creating a channel in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class CreateChannelUserListComponent extends SelectUserListComponent<UserInfo> {
    @NonNull
    private CreateChannelUserListAdapter adapter = new CreateChannelUserListAdapter();

    /**
     * Returns the user list adapter when creating a channel.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    @Override
    protected CreateChannelUserListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the user list adapter when creating a channel to provide child views on demand. The default is {@code new CreateChannelUserListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends CreateChannelUserListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
