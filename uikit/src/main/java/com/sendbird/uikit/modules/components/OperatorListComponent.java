package com.sendbird.uikit.modules.components;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.adapter.OperatorListAdapter;

/**
 * This class creates and performs a view corresponding the operator list area in Sendbird UIKit.
 *
 * @since 3.0.0
 */
public class OperatorListComponent extends UserTypeListComponent<User> {

    @NonNull
    private OperatorListAdapter adapter = new OperatorListAdapter();

    /**
     * Returns the operator list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @SuppressLint("KotlinPropertyAccess")
    @NonNull
    @Override
    protected OperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the operator list  adapter to provide child views on demand. The default is {@code new OperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends OperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
