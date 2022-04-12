package com.sendbird.uikit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.Member;
import com.sendbird.uikit.activities.adapter.PromoteOperatorListAdapter;

/**
 * This class creates and performs a view corresponding the member list area when promoting members to operators in Sendbird UIKit.
 *
 * @since 3.0.0
 */
public class PromoteOperatorListComponent extends SelectUserListComponent<Member> {
    @NonNull
    private PromoteOperatorListAdapter adapter = new PromoteOperatorListAdapter();

    /**
     * Returns the member list adapter when promoting members to operators.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @NonNull
    @Override
    protected PromoteOperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the member list adapter when promoting members to operators to provide child views on demand. The default is {@code new PromoteOperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends PromoteOperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
