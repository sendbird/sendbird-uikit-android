package com.sendbird.uikit.internal.queries

import com.sendbird.android.SendbirdChat.createMutedUserListQuery
import com.sendbird.android.channel.ChannelType
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.MutedUserListQueryParams
import com.sendbird.android.user.RestrictedUser
import com.sendbird.android.user.User
import com.sendbird.android.user.query.MutedUserListQuery
import com.sendbird.uikit.interfaces.OnListResultHandler
import com.sendbird.uikit.interfaces.PagedQueryHandler

internal class MutedUserListQuery(private val channelType: ChannelType, private val channelUrl: String) :
    PagedQueryHandler<User> {
    private var query: MutedUserListQuery? = null
    override fun loadInitial(handler: OnListResultHandler<User>) {
        query = createMutedUserListQuery(MutedUserListQueryParams(channelType, channelUrl).apply { limit = 30 })
        loadMore(handler)
    }

    override fun loadMore(handler: OnListResultHandler<User>) {
        query?.next { list: List<RestrictedUser>?, e: SendbirdException? ->
            handler.onResult(if (list != null) ArrayList<User>(list) else null, e)
        } ?: handler.onResult(null, SendbirdException("loadInitial must be called first."))
    }

    override fun hasMore(): Boolean {
        return query?.hasNext ?: false
    }
}
