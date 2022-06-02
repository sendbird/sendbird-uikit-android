package com.sendbird.uikit.customsample;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sendbird.uikit.consts.CreatableChannelType;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomBannedUserListFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelListFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomChannelSettingsFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomCreateChannelFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomInviteUserFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomMemberListFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomMessageSearchFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomModerationFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomMutedMemberListFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomOperatorListFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomParticipantsListFragment;
import com.sendbird.uikit.customsample.groupchannel.fragments.CustomPromoteOperatorsFragment;
import com.sendbird.uikit.customsample.openchannel.CustomOpenChannelSettingsFragment;
import com.sendbird.uikit.fragments.BannedUserListFragment;
import com.sendbird.uikit.fragments.ChannelFragment;
import com.sendbird.uikit.fragments.ChannelListFragment;
import com.sendbird.uikit.fragments.ChannelSettingsFragment;
import com.sendbird.uikit.fragments.CreateChannelFragment;
import com.sendbird.uikit.fragments.InviteUserFragment;
import com.sendbird.uikit.fragments.MemberListFragment;
import com.sendbird.uikit.fragments.MessageSearchFragment;
import com.sendbird.uikit.fragments.ModerationFragment;
import com.sendbird.uikit.fragments.MutedMemberListFragment;
import com.sendbird.uikit.fragments.OpenChannelSettingsFragment;
import com.sendbird.uikit.fragments.OperatorListFragment;
import com.sendbird.uikit.fragments.ParticipantListFragment;
import com.sendbird.uikit.fragments.PromoteOperatorFragment;
import com.sendbird.uikit.fragments.UIKitFragmentFactory;

/**
 * UIKit fragment factory implementation to provide customized fragments.
 */
public class CustomFragmentFactory extends UIKitFragmentFactory {
    @NonNull
    @Override
    public Fragment newChannelListFragment(@NonNull Bundle args) {
        final Fragment fragment = new CustomChannelListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newChannelFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newMessageSearchFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomMessageSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newCreateChannelFragment(@NonNull CreatableChannelType channelType, @NonNull Bundle args) {
        final Fragment fragment = new CustomCreateChannelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newChannelSettingsFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomChannelSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newInviteUserFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomInviteUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newPromoteOperatorFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomPromoteOperatorsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newModerationFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomModerationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newMemberListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomMemberListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newBannedUserListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomBannedUserListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newOperatorListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomOperatorListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newMutedMemberListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomMutedMemberListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newParticipantListFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final Fragment fragment = new CustomParticipantsListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Fragment newOpenChannelSettingsFragment(@NonNull String channelUrl, @NonNull Bundle args) {
        final CustomOpenChannelSettingsFragment fragment = new CustomOpenChannelSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
