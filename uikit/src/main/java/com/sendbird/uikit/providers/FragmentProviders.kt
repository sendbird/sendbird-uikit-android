package com.sendbird.uikit.providers

import com.sendbird.uikit.fragments.*
import com.sendbird.uikit.interfaces.providers.*

/**
 * Create a Fragment provider.
 * In situations where you need to create a fragment, create the fragment through the following providers.
 * If you need to use Custom Fragment, change the provider
 *
 * @since 3.9.0
 */
object FragmentProviders {

    /**
     * Returns the ChannelListFragment provider.
     *
     * @return The [ChannelListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var channelList = ChannelListFragmentProvider { args ->
        ChannelListFragment.Builder().withArguments(args).setUseHeader(true).build()
    }

    /**
     * Returns the ChannelFragment provider.
     *
     * @return The [ChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var channel = ChannelFragmentProvider { channelUrl, args ->
        ChannelFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the OpenChannelFragment provider.
     *
     * @return The [OpenChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannel = OpenChannelFragmentProvider { channelUrl, args ->
        OpenChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the CreateChannelFragment provider.
     *
     * @return The [CreateChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var createChannel = CreateChannelFragmentProvider { channelType, args ->
        CreateChannelFragment.Builder(channelType).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the CreateOpenChannelFragment provider.
     *
     * @return The [CreateOpenChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var createOpenChannel = CreateOpenChannelFragmentProvider { args ->
        CreateOpenChannelFragment.Builder().withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(true)
            .build()
    }

    /**
     * Returns the ChannelSettingsFragment provider.
     *
     * @return The [ChannelSettingsFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var channelSettings = ChannelSettingsFragmentProvider { channelUrl, args ->
        ChannelSettingsFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the OpenChannelSettingsFragment provider.
     *
     * @return The [OpenChannelSettingsFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelSettings = OpenChannelSettingsFragmentProvider { channelUrl, args ->
        OpenChannelSettingsFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the InviteUserFragment provider.
     *
     * @return The [InviteUserFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var inviteUser = InviteUserFragmentProvider { channelUrl, args ->
        InviteUserFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the RegisterOperatorFragment provider.
     *
     * @return The [RegisterOperatorFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var registerOperator = RegisterOperatorFragmentProvider { channelUrl, args ->
        RegisterOperatorFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build() as RegisterOperatorFragment // for backward compatibility
    }

    /**
     * Returns the OpenChannelRegisterOperatorFragment provider.
     *
     * @return The [OpenChannelRegisterOperatorFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelRegisterOperator = OpenChannelRegisterOperatorFragmentProvider { channelUrl, args ->
        OpenChannelRegisterOperatorFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    /**
     * Returns the ModerationFragment provider.
     *
     * @return The [ModerationFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var moderation = ModerationFragmentProvider { channelUrl, args ->
        ModerationFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the OpenChannelModerationFragment provider.
     *
     * @return The [OpenChannelModerationFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelModeration = OpenChannelModerationFragmentProvider { channelUrl, args ->
        OpenChannelModerationFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    /**
     * Returns the MemberListFragment provider.
     *
     * @return The [MemberListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var memberList = MemberListFragmentProvider { channelUrl, args ->
        MemberListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(true)
            .build()
    }

    /**
     * Returns the BannedUserListFragment provider.
     *
     * @return The [BannedUserListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var bannedUserList = BannedUserListFragmentProvider { channelUrl, args ->
        BannedUserListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(false)
            .build()
    }

    /**
     * Returns the OpenChannelBannedUserListFragment provider.
     *
     * @return The [OpenChannelBannedUserListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelBannedUserList = OpenChannelBannedUserListFragmentProvider { channelUrl, args ->
        OpenChannelBannedUserListFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    /**
     * Returns the MutedMemberListFragment provider.
     *
     * @return The [MutedMemberListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var mutedMemberList = MutedMemberListFragmentProvider { channelUrl, args ->
        MutedMemberListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(false)
            .build()
    }

    /**
     * Returns the OpenChannelMutedParticipantListFragment provider.
     *
     * @return The [OpenChannelMutedParticipantListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelMutedParticipantList = OpenChannelMutedParticipantListFragmentProvider { channelUrl, args ->
        OpenChannelMutedParticipantListFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    /**
     * Returns the OperatorListFragment provider.
     *
     * @return The [OperatorListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var operatorList = OperatorListFragmentProvider { channelUrl, args ->
        OperatorListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .setUseHeaderRightButton(true)
            .build()
    }

    /**
     * Returns the OpenChannelOperatorListFragment provider.
     *
     * @return The [OpenChannelOperatorListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelOperatorList = OpenChannelOperatorListFragmentProvider { channelUrl, args ->
        OpenChannelOperatorListFragment.Builder(channelUrl).withArguments(args)
            .build()
    }

    /**
     * Returns the MessageSearchFragment provider.
     *
     * @return The [MessageSearchFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var messageSearch = MessageSearchFragmentProvider { channelUrl, args ->
        MessageSearchFragment.Builder(channelUrl).withArguments(args)
            .setUseSearchBar(true)
            .build()
    }

    /**
     * Returns the MessageThreadFragment provider.
     *
     * @return The [MessageThreadFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var messageThread = MessageThreadFragmentProvider { channelUrl, message, args ->
        MessageThreadFragment.Builder(channelUrl, message).setStartingPoint(0L)
            .setUseHeader(true)
            .withArguments(args)
            .build()
    }

    /**
     * Returns the ParticipantListFragment provider.
     *
     * @return The [ParticipantListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var participantList = ParticipantListFragmentProvider { channelUrl, args ->
        ParticipantListFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the ChannelPushSettingFragment provider.
     *
     * @return The [ChannelPushSettingFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var channelPushSetting = ChannelPushSettingFragmentProvider { channelUrl, args ->
        ChannelPushSettingFragment.Builder(channelUrl).withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the OpenChannelListFragment provider.
     *
     * @return The [OpenChannelListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelList = OpenChannelListFragmentProvider { args ->
        OpenChannelListFragment.Builder().withArguments(args)
            .setUseHeader(true)
            .build()
    }

    /**
     * Returns the FeedNotificationChannelFragment provider.
     *
     * @return The [FeedNotificationChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var feedNotificationChannel = FeedNotificationChannelFragmentProvider { channelUrl, args ->
        FeedNotificationChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeaderLeftButton(true)
            .build()
    }

    /**
     * Returns the ChatNotificationChannelFragment provider.
     *
     * @return The [ChatNotificationChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    var chatNotificationChannel = ChatNotificationChannelFragmentProvider { channelUrl, args ->
        ChatNotificationChannelFragment.Builder(channelUrl)
            .withArguments(args)
            .setUseHeaderLeftButton(true)
            .build()
    }
}
