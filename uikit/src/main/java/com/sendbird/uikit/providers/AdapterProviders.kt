package com.sendbird.uikit.providers

import com.sendbird.uikit.activities.adapter.*
import com.sendbird.uikit.interfaces.providers.BannedUserListAdapterProvider
import com.sendbird.uikit.interfaces.providers.ChannelListAdapterProvider
import com.sendbird.uikit.interfaces.providers.CreateChannelUserListAdapterProvider
import com.sendbird.uikit.interfaces.providers.InviteUserListAdapterProvider
import com.sendbird.uikit.interfaces.providers.MemberListAdapterProvider
import com.sendbird.uikit.interfaces.providers.MessageListAdapterProvider
import com.sendbird.uikit.interfaces.providers.MessageSearchAdapterProvider
import com.sendbird.uikit.interfaces.providers.MutedMemberListAdapterProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelBannedUserListAdapterProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelListAdapterProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelMessageListAdapterProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelMutedParticipantListAdapterProvider
import com.sendbird.uikit.interfaces.providers.OpenChannelRegisterOperatorListAdapterProvider
import com.sendbird.uikit.interfaces.providers.OperatorListAdapterProvider
import com.sendbird.uikit.interfaces.providers.ParticipantListAdapterProvider
import com.sendbird.uikit.interfaces.providers.RegisterOperatorListAdapterProvider
import com.sendbird.uikit.interfaces.providers.ThreadListAdapterProvider

/**
 * A set of Providers that provide a RecyclerView.Adapter that binds to a RecyclerView among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object AdapterProviders {
    /**
     * Returns the MessageListAdapter provider.
     *
     * @return The [MessageListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var messageList = MessageListAdapterProvider { channel, messageListUIParams ->
        MessageListAdapter(channel, messageListUIParams)
    }

    /**
     * Returns the BannedUserListAdapter provider.
     *
     * @return The [BannedUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var bannedUserList = BannedUserListAdapterProvider { BannedUserListAdapter() }

    /**
     * Returns the ChannelListAdapter provider.
     *
     * @return The [ChannelListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var channelList = ChannelListAdapterProvider { uiParams ->
        ChannelListAdapter(null, uiParams)
    }

    /**
     * Returns the CreateChannelUserListAdapter provider.
     *
     * @return The [CreateChannelUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var createChannelUserList = CreateChannelUserListAdapterProvider { CreateChannelUserListAdapter() }

    /**
     * Returns the InviteChannelFragment provider.
     *
     * @return The [InviteUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var inviteUserList = InviteUserListAdapterProvider { InviteUserListAdapter() }

    /**
     * Returns the MemberListAdapter provider.
     *
     * @return The [MemberListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var memberList = MemberListAdapterProvider { MemberListAdapter() }

    /**
     * Returns the MessageSearchAdapter provider.
     *
     * @return The [MessageSearchAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var messageSearch = MessageSearchAdapterProvider { MessageSearchAdapter() }

    /**
     * Returns the MutedMemberListAdapter provider.
     *
     * @return The [MutedMemberListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var mutedMemberList = MutedMemberListAdapterProvider { MutedMemberListAdapter() }

    /**
     * Returns the OpenChannelBannedUserListAdapter provider.
     *
     * @return The [OpenChannelBannedUserListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelBannedUserList = OpenChannelBannedUserListAdapterProvider {
        OpenChannelBannedUserListAdapter()
    }

    /**
     * Returns the OpenChannelListAdapter provider.
     *
     * @return The [OpenChannelListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelList = OpenChannelListAdapterProvider { OpenChannelListAdapter() }

    /**
     * Returns the OpenChannelMessageListAdapter provider.
     *
     * @return The [OpenChannelMessageListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelMessageList = OpenChannelMessageListAdapterProvider { messageListUIParams ->
        OpenChannelMessageListAdapter(null, messageListUIParams)
    }

    /**
     * Returns the OpenChannelMutedParticipantListAdapter provider.
     *
     * @return The [OpenChannelMutedParticipantListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelMutedParticipantList = OpenChannelMutedParticipantListAdapterProvider { OpenChannelMutedParticipantListAdapter() }

    /**
     * Returns the OpenChannelRegisterOperatorListAdapter provider.
     *
     * @return The [OpenChannelRegisterOperatorListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var openChannelRegisterOperatorList = OpenChannelRegisterOperatorListAdapterProvider { channel ->
        OpenChannelRegisterOperatorListAdapter(channel)
    }

    /**
     * Returns the OperatorListAdapter provider.
     *
     * @return The [OperatorListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var operatorList = OperatorListAdapterProvider { OperatorListAdapter() }

    /**
     * Returns the ParticipantListAdapter provider.
     *
     * @return The [ParticipantListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var participantList = ParticipantListAdapterProvider { ParticipantListAdapter() }

    /**
     * Returns the RegisterOperatorListAdapter provider.
     *
     * @return The [RegisterOperatorListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var registerOperatorList = RegisterOperatorListAdapterProvider { RegisterOperatorListAdapter() }

    /**
     * Returns the ThreadListAdapter provider.
     *
     * @return The [ThreadListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    var threadList = ThreadListAdapterProvider { messageListUIParams ->
        ThreadListAdapter(null, messageListUIParams)
    }
}
