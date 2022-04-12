package com.sendbird.uikit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.consts.CreatableChannelType;

/**
 * Create a new Fragment.
 * Each screen provided at UIKit creates a fragment via this Factory.
 * To use custom fragment, not a default fragment, you must inherit this Factory.
 * Extended Factory must be registered in SDK through {@link com.sendbird.uikit.SendbirdUIKit#setUIKitFragmentFactory(UIKitFragmentFactory)} method.
 */
public class UIKitFragmentFactory {

    /**
     * Returns the ChannelListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link ChannelListFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newChannelListFragment(@NonNull Bundle args) {
        return new ChannelListFragment.Builder()
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the ChannelFragment.
     *
     * @param channelUrl the channel url,
     * @param args       the arguments supplied when the fragment was instantiated.
     * @return The {@link ChannelFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newChannelFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new ChannelFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the ChannelSettingsFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link ChannelSettingsFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newChannelSettingsFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new ChannelSettingsFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the CreateChannelFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link CreateChannelFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newCreateChannelFragment(@NonNull CreatableChannelType channelType, @NonNull Bundle args) {
        return new CreateChannelFragment.Builder(channelType)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the InviteUserFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link InviteUserFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newInviteUserFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new InviteUserFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the PromoteOperatorFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link PromoteOperatorFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newPromoteOperatorFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new PromoteOperatorFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the MessageSearchFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link MessageSearchFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newMessageSearchFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new MessageSearchFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseSearchBar(true)
                .build();
    }

    /**
     * Returns the ModerationFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link ModerationFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newModerationFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new ModerationFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the MemberListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link MemberListFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newMemberListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new MemberListFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
                .build();
    }

    /**
     * Returns the BannedUserListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link BannedUserListFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newBannedUserListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new BannedUserListFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(false)
                .build();
    }

    /**
     * Returns the OperatorListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OperatorListFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newOperatorListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OperatorListFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
                .build();
    }

    /**
     * Returns the MutedMemberListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link MutedMemberListFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newMutedMemberListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new MutedMemberListFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .setUseHeaderRightButton(false)
                .build();
    }

    /**
     * Returns the ParticipantListFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link ParticipantListFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newParticipantListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new ParticipantListFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the OpenChannelSettingsFragment.
     *
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelSettingsFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newOpenChannelSettingsFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OpenChannelSettingsFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }
}
