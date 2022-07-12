package com.sendbird.uikit.modules.components;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.adapter.ParticipantListAdapter;

/**
 * This class creates and performs a view corresponding the participant list area in Sendbird UIKit.
 *
 * @since 3.0.0
 */
public class ParticipantListComponent extends UserTypeListComponent<User> {
    @NonNull
    private ParticipantListAdapter adapter = new ParticipantListAdapter();

    /**
     * Returns the participant list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @SuppressLint("KotlinPropertyAccess")
    @NonNull
    @Override
    protected ParticipantListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the participant list  adapter to provide child views on demand. The default is {@code new ParticipantsListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends ParticipantListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
