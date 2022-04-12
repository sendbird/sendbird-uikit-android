package com.sendbird.uikit.modules.components;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.sendbird.android.Member;
import com.sendbird.uikit.activities.adapter.MemberListAdapter;

/**
 * This class creates and performs a view corresponding the member list area in Sendbird UIKit.
 *
 * @since 3.0.0
 */
public class MemberListComponent extends UserTypeListComponent<Member> {
    @NonNull
    private MemberListAdapter adapter = new MemberListAdapter();

    /**
     * Returns the member list adapter.
     *
     * @return The adapter applied to this list component
     * @since 3.0.0
     */
    @SuppressLint("KotlinPropertyAccess")
    @NonNull
    @Override
    protected MemberListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the member list  adapter to provide child views on demand. The default is {@code new MemberListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * @since 3.0.0
     */
    public <T extends MemberListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
