package com.sendbird.uikit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.adapter.OpenChannelRegisterOperatorListAdapter;

/**
 * This class creates and performs a view corresponding the participant list area when registering operators in Sendbird UIKit.
 *
 * since 3.1.0
 */
public class OpenChannelRegisterOperatorListComponent extends SelectUserListComponent<User> {
    @NonNull
    private OpenChannelRegisterOperatorListAdapter adapter = new OpenChannelRegisterOperatorListAdapter(null);

    /**
     * Returns the participant list adapter when registering operators.
     *
     * @return The adapter applied to this list component
     * since 3.1.0
     */
    @NonNull
    @Override
    protected OpenChannelRegisterOperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the participant list adapter when registering operators to provide child views on demand. The default is {@code new RegisterOperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.1.0
     */
    public <T extends OpenChannelRegisterOperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
