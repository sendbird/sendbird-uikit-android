package com.sendbird.uikit.modules.components;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.sendbird.android.Member;
import com.sendbird.uikit.activities.adapter.MutedMemberListAdapter;

/**
 * This class creates and performs a view corresponding the muted member list area in Sendbird UIKit.
 *
 * @since 3.0.0
 */
public class MutedMemberListComponent extends UserTypeListComponent<Member> {
    @NonNull
    private MutedMemberListAdapter adapter = new MutedMemberListAdapter();

    /**
     * Sets the muted member list  adapter to provide child views on demand. The default is {@code new MutedMemberListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends MutedMemberListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }

    /**
     * Returns the muted member list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @SuppressLint("KotlinPropertyAccess")
    @NonNull
    @Override
    protected MutedMemberListAdapter getAdapter() {
        return adapter;
    }
}
