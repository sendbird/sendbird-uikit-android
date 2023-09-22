package com.sendbird.uikit.providers

import androidx.lifecycle.ViewModelProvider
import com.sendbird.uikit.interfaces.providers.BannedUserListViewModelProvider
import com.sendbird.uikit.interfaces.providers.ChannelListViewModelProvider
import com.sendbird.uikit.interfaces.providers.ChannelPushSettingViewModelProvider
import com.sendbird.uikit.interfaces.providers.ChannelSettingsViewModelProvider
import com.sendbird.uikit.interfaces.providers.ChannelViewModelProvider
import com.sendbird.uikit.interfaces.providers.ChatNotificationChannelViewModelProvider
import com.sendbird.uikit.interfaces.providers.CreateChannelViewModelProvider
import com.sendbird.uikit.interfaces.providers.CreateOpenChannelViewModelProvider
import com.sendbird.uikit.interfaces.providers.FeedNotificationChannelViewModelProvider
import com.sendbird.uikit.interfaces.providers.InviteUserViewModelProvider
import com.sendbird.uikit.interfaces.providers.MemberListViewModelProvider
import com.sendbird.uikit.interfaces.providers.MessageSearchViewModelProvider
import com.sendbird.uikit.interfaces.providers.MessageThreadViewModelProvider
import com.sendbird.uikit.interfaces.providers.ModerationViewModelProvider
import com.sendbird.uikit.interfaces.providers.MutedMemberListViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelBannedUserListViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelListViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelModerationViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelMutedParticipantListViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelOperatorListViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelRegisterOperatorViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelSettingsViewModelProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelViewModelProvider
import com.sendbird.uikit.interfaces.providers.OperatorListViewModelProvider
import com.sendbird.uikit.interfaces.providers.ParticipantViewModelProvider
import com.sendbird.uikit.interfaces.providers.RegisterOperatorViewModelProvider
import com.sendbird.uikit.vm.*

/**
 * A set of Providers that provide a [BaseViewModel] that binds to a Fragment among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object ViewModelProviders {
    /**
     * Returns the ChannelListViewModel provider.
     *
     * @return The [ChannelListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelList = ChannelListViewModelProvider { owner, query ->
        ViewModelProvider(owner, ViewModelFactory(query))[ChannelListViewModel::class.java]
    }

    /**
     * Returns the ChannelViewModel provider.
     *
     * @return The [ChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channel = ChannelViewModelProvider { owner, channelUrl, params, channelConfig ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, params, channelConfig)
        )[channelUrl, ChannelViewModel::class.java]
    }

    /**
     * Returns the OpenChannelViewModel provider.
     *
     * @return The [OpenChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannel = OpenChannelViewModelProvider { owner, channelUrl, params ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, params)
        )[channelUrl, OpenChannelViewModel::class.java]
    }

    /**
     * Returns the CreateChannelViewModel provider.
     *
     * @return The [CreateChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var createChannel = CreateChannelViewModelProvider { owner, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(pagedQueryHandler)
        )[CreateChannelViewModel::class.java]
    }

    /**
     * Returns the CreateOpenChannelViewModel provider.
     *
     * @return The [CreateOpenChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var createOpenChannel = CreateOpenChannelViewModelProvider { owner ->
        ViewModelProvider(
            owner,
            ViewModelFactory()
        )[CreateOpenChannelViewModel::class.java]
    }

    /**
     * Returns the ChannelSettingsViewModel provider.
     *
     * @return The [ChannelSettingsViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelSettings = ChannelSettingsViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, ChannelSettingsViewModel::class.java]
    }

    /**
     * Returns the OpenChannelSettingsViewModel provider.
     *
     * @return The [OpenChannelSettingsViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelSettings = OpenChannelSettingsViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, OpenChannelSettingsViewModel::class.java]
    }

    /**
     * Returns the InviteUserViewModel provider.
     *
     * @return The [InviteUserViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var inviteUser = InviteUserViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[InviteUserViewModel::class.java]
    }

    /**
     * Returns the RegisterOperatorViewModel provider.
     *
     * @return The [RegisterOperatorViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var registerOperator = RegisterOperatorViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[RegisterOperatorViewModel::class.java]
    }

    /**
     * Returns the OpenChannelRegisterOperatorViewModel provider.
     *
     * @return The [OpenChannelRegisterOperatorViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelRegisterOperator =
        OpenChannelRegisterOperatorViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[OpenChannelRegisterOperatorViewModel::class.java]
        }

    /**
     * Returns the ModerationViewModel provider.
     *
     * @return The [ModerationViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var moderation = ModerationViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, ModerationViewModel::class.java]
    }

    /**
     * Returns the OpenChannelModerationViewModel provider.
     *
     * @return The [OpenChannelModerationViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelModeration = OpenChannelModerationViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, OpenChannelModerationViewModel::class.java]
    }

    /**
     * Returns the MemberListViewModel provider.
     *
     * @return The [MemberListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var memberList = MemberListViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, MemberListViewModel::class.java]
    }

    /**
     * Returns the BannedUserListViewModel provider.
     *
     * @return The [BannedUserListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var bannedUserList = BannedUserListViewModelProvider { owner, channelUrl, channelType ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, channelType)
        )[channelUrl, BannedUserListViewModel::class.java]
    }

    /**
     * Returns the OpenChannelBannedUserListViewModel provider.
     *
     * @return The [OpenChannelBannedUserListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelBannedUserList = OpenChannelBannedUserListViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, OpenChannelBannedUserListViewModel::class.java]
    }

    /**
     * Returns the MutedMemberListViewModel provider.
     *
     * @return The [MutedMemberListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var mutedMemberList = MutedMemberListViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, MutedMemberListViewModel::class.java]
    }

    /**
     * Returns the OpenChannelMutedParticipantListViewModel provider.
     *
     * @return The [OpenChannelMutedParticipantListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelMutedParticipantList =
        OpenChannelMutedParticipantListViewModelProvider { owner, channelUrl, pagedQueryHandler ->
            ViewModelProvider(
                owner,
                ViewModelFactory(channelUrl, pagedQueryHandler)
            )[channelUrl, OpenChannelMutedParticipantListViewModel::class.java]
        }

    /**
     * Returns the OperatorListViewModel provider.
     *
     * @return The [OperatorListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var operatorList = OperatorListViewModelProvider { owner, channelUrl, channelType, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, channelType, pagedQueryHandler)
        )[channelUrl, OperatorListViewModel::class.java]
    }

    /**
     * Returns the OpenChannelOperatorListViewModel provider.
     *
     * @return The [OpenChannelOperatorListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelOperatorList = OpenChannelOperatorListViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[channelUrl, OpenChannelOperatorListViewModel::class.java]
    }

    /**
     * Returns the MessageSearchViewModel provider.
     *
     * @return The [MessageSearchViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var messageSearch = MessageSearchViewModelProvider { owner, channelUrl, query ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, query)
        )[channelUrl, MessageSearchViewModel::class.java]
    }

    /**
     * Returns the MessageThreadViewModel provider.
     *
     * @return The [MessageThreadViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var messageThread = MessageThreadViewModelProvider { owner, channelUrl, parentMessage, params ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, parentMessage, params)
        )[channelUrl, MessageThreadViewModel::class.java]
    }

    /**
     * Returns the ParticipantViewModel provider.
     *
     * @return The [ParticipantViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var participantList = ParticipantViewModelProvider { owner, channelUrl, pagedQueryHandler ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl, pagedQueryHandler)
        )[channelUrl, ParticipantViewModel::class.java]
    }

    /**
     * Returns the ChannelPushSettingViewModel provider.
     *
     * @return The [ChannelPushSettingViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelPushSetting = ChannelPushSettingViewModelProvider { owner, channelUrl ->
        ViewModelProvider(
            owner,
            ViewModelFactory(channelUrl)
        )[channelUrl, ChannelPushSettingViewModel::class.java]
    }

    /**
     * Returns the OpenChannelListViewModel provider.
     *
     * @return The [OpenChannelListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelList = OpenChannelListViewModelProvider { owner, params ->
        ViewModelProvider(
            owner,
            ViewModelFactory(params)
        )[OpenChannelListViewModel::class.java]
    }

    /**
     * Returns the FeedNotificationChannelViewModel provider.
     *
     * @return The [FeedNotificationChannelViewModel].
     * @since 3.9.0
     */
    @JvmStatic
    internal var feedNotificationChannel = FeedNotificationChannelViewModelProvider { owner, channelUrl, params ->
        ViewModelProvider(
            owner,
            NotificationViewModelFactory(channelUrl, params)
        )[channelUrl, FeedNotificationChannelViewModel::class.java]
    }

    /**
     * Returns the ChatNotificationChannelViewModel provider.
     *
     * @return The [ChatNotificationChannelViewModel].
     * @since 3.9.0
     */
    @JvmStatic
    internal var chatNotificationChannel = ChatNotificationChannelViewModelProvider { owner, channelUrl, params ->
        ViewModelProvider(
            owner,
            NotificationViewModelFactory(channelUrl, params)
        )[channelUrl, ChatNotificationChannelViewModel::class.java]
    }
}
