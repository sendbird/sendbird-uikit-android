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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelType the channel type to be created.
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
     * @param channelUrl the channel url for the target channel.
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
     * Returns the RegisterOperatorFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link RegisterOperatorFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newRegisterOperatorFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new RegisterOperatorFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the MessageSearchFragment.
     *
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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
     * @param channelUrl the channel url for the target channel.
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

    /**
     * Returns the OpenChannelSettingsFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelSettingsFragment}
     * @since 3.0.0
     */
    @NonNull
    public Fragment newChannelPushSettingFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new ChannelPushSettingFragment.Builder(channelUrl)
                .withArguments(args)
                .setUseHeader(true)
                .build();
    }

    /**
     * Returns the OpenChannelModerationFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelModerationFragment}
     * @since 3.1.0
     */
    @NonNull
    public Fragment newOpenChannelModerationFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OpenChannelModerationFragment.Builder(channelUrl)
                .withArguments(args)
                .build();
    }

    /**
     * Returns the OpenChannelRegisterOperatorFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelRegisterOperatorFragment}
     * @since 3.1.0
     */
    @NonNull
    public Fragment newOpenChannelRegisterOperatorFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OpenChannelRegisterOperatorFragment.Builder(channelUrl)
                .withArguments(args)
                .build();
    }

    /**
     * Returns the OpenChannelOperatorListFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelOperatorListFragment}
     * @since 3.1.0
     */
    @NonNull
    public Fragment newOpenChannelOperatorListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OpenChannelOperatorListFragment.Builder(channelUrl)
                .withArguments(args)
                .build();
    }

    /**
     * Returns the OpenChannelMutedParticipantListFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelMutedParticipantListFragment}
     * @since 3.1.0
     */
    @NonNull
    public Fragment newOpenChannelMutedParticipantListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OpenChannelMutedParticipantListFragment.Builder(channelUrl)
                .withArguments(args)
                .build();
    }

    /**
     * Returns the OpenChannelBannedUserListFragment.
     *
     * @param channelUrl the channel url for the target channel.
     * @param args the arguments supplied when the fragment was instantiated.
     * @return The {@link OpenChannelBannedUserListFragment}
     * @since 3.1.0
     */
    @NonNull
    public Fragment newOpenChannelBannedUserListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        return new OpenChannelBannedUserListFragment.Builder(channelUrl)
                .withArguments(args)
                .build();
    }
}
