package com.sendbird.uikit.providers

import com.sendbird.uikit.interfaces.providers.*
import com.sendbird.uikit.internal.ui.notifications.ChatNotificationChannelModule
import com.sendbird.uikit.internal.ui.notifications.FeedNotificationChannelModule
import com.sendbird.uikit.modules.*

/**
 * UIKit for Android, you need a module and components to create a view.
 * Components are the smallest unit of customizable views that can make up a whole screen and the module coordinates these components to be shown as the fragment's view.
 * Each module also has its own customizable style per screen.
 * A set of Providers that provide a Module that binds to a Fragment among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object ModuleProviders {

    /**
     * Returns the ChannelListModule provider.
     *
     * @return The [ChannelListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelList = ChannelListModuleProvider { context, _ -> ChannelListModule(context) }

    /**
     * Returns the ChannelModule provider.
     *
     * @return The [ChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channel = ChannelModuleProvider { context, _ -> ChannelModule(context) }

    /**
     * Returns the OpenChannelModule provider.
     *
     * @return The [OpenChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannel = OpenChannelModuleProvider { context, _ -> OpenChannelModule(context) }

    /**
     * Returns the CreateChannelModule provider.
     *
     * @return The [CreateChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var createChannel = CreateChannelModuleProvider { context, _ -> CreateChannelModule(context) }

    /**
     * Returns the CreateOpenChannelModule provider.
     *
     * @return The [CreateOpenChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var createOpenChannel = CreateOpenChannelModuleProvider { context, _ -> CreateOpenChannelModule(context) }

    /**
     * Returns the ChannelSettingsModule provider.
     *
     * @return The [ChannelSettingsModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelSettings = ChannelSettingsModuleProvider { context, _ -> ChannelSettingsModule(context) }

    /**
     * Returns the OpenChannelSettingsModule provider.
     *
     * @return The [OpenChannelSettingsModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelSettings = OpenChannelSettingsModuleProvider { context, _ -> OpenChannelSettingsModule(context) }

    /**
     * Returns the InviteUserModule provider.
     *
     * @return The [InviteUserModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var inviteUser = InviteUserModuleProvider { context, _ -> InviteUserModule(context) }

    /**
     * Returns the RegisterOperatorModule provider.
     *
     * @return The [RegisterOperatorModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var registerOperator = RegisterOperatorModuleProvider { context, _ -> RegisterOperatorModule(context) }

    /**
     * Returns the OpenChannelRegisterOperatorModule provider.
     *
     * @return The [OpenChannelRegisterOperatorModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelRegisterOperator =
        OpenChannelRegisterOperatorModuleProvider { context, _ -> OpenChannelRegisterOperatorModule(context) }

    /**
     * Returns the ModerationModule provider.
     *
     * @return The [ModerationModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var moderation = ModerationModuleProvider { context, _ -> ModerationModule(context) }

    /**
     * Returns the OpenChannelModerationModule provider.
     *
     * @return The [OpenChannelModerationModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelModeration =
        OpenChannelModerationModuleProvider { context, _ -> OpenChannelModerationModule(context) }

    /**
     * Returns the MemberListModule provider.
     *
     * @return The [MemberListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var memberList = MemberListModuleProvider { context, _ -> MemberListModule(context) }

    /**
     * Returns the BannedUserListModule provider.
     *
     * @return The [BannedUserListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var bannedUserList = BannedUserListModuleProvider { context, _ -> BannedUserListModule(context) }

    /**
     * Returns the OpenChannelBannedUserListModule provider.
     *
     * @return The [OpenChannelBannedUserListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelBannedUserList =
        OpenChannelBannedUserListModuleProvider { context, _ -> OpenChannelBannedUserListModule(context) }

    /**
     * Returns the MutedMemberListModule provider.
     *
     * @return The [MutedMemberListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var mutedMemberList = MutedMemberListModuleProvider { context, _ -> MutedMemberListModule(context) }

    /**
     * Returns the OpenChannelMutedParticipantListModule provider.
     *
     * @return The [OpenChannelMutedParticipantListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelMutedParticipantList =
        OpenChannelMutedParticipantListModuleProvider { context, _ -> OpenChannelMutedParticipantListModule(context) }

    /**
     * Returns the OperatorListModule provider.
     *
     * @return The [OperatorListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var operatorList = OperatorListModuleProvider { context, _ -> OperatorListModule(context) }

    /**
     * Returns the OpenChannelOperatorListModule provider.
     *
     * @return The [OpenChannelOperatorListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelOperatorList =
        OpenChannelOperatorListModuleProvider { context, _ -> OpenChannelOperatorListModule(context) }

    /**
     * Returns the MessageSearchModule provider.
     *
     * @return The [MessageSearchModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var messageSearch = MessageSearchModuleProvider { context, _ -> MessageSearchModule(context) }

    /**
     * Returns the MessageThreadModule provider.
     *
     * @return The [MessageThreadModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var messageThread = MessageThreadModuleProvider { context, _, message -> MessageThreadModule(context, message) }

    /**
     * Returns the ParticipantListModule provider.
     *
     * @return The [ParticipantListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var participantList = ParticipantListModuleProvider { context, _ -> ParticipantListModule(context) }

    /**
     * Returns the ChannelPushSettingModule provider.
     *
     * @return The [ChannelPushSettingModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelPushSetting = ChannelPushSettingModuleProvider { context, _ -> ChannelPushSettingModule(context) }

    /**
     * Returns the OpenChannelListModule provider.
     *
     * @return The [OpenChannelListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelList = OpenChannelListModuleProvider { context, _ -> OpenChannelListModule(context) }

    /**
     * Returns the OpenChannelParticipantListModule provider.
     *
     * @return The [FeedNotificationChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    internal var feedNotificationChannel = FeedNotificationChannelModuleProvider { context, _, config ->
        FeedNotificationChannelModule(context, config)
    }

    /**
     * Returns the ChatNotificationChannelModule provider.
     *
     * @return The [ChatNotificationChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    internal var chatNotificationChannel = ChatNotificationChannelModuleProvider { context, _, config ->
        ChatNotificationChannelModule(context, config)
    }
}
