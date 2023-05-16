package com.sendbird.uikit.internal.wrappers

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.android.handler.GroupChannelsCallbackHandler

internal interface GroupChannelCollectionWrapper {
    fun setGroupChannelCollectionHandler(handler: GroupChannelCollectionHandler?)
    fun loadMore(handler: GroupChannelsCallbackHandler)
    fun getChannelList(): List<GroupChannel>
    fun getHasMore(): Boolean
    fun dispose()
}
