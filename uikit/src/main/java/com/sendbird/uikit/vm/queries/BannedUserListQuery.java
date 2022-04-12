package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

public class BannedUserListQuery implements PagedQueryHandler<User> {
    private com.sendbird.android.BannedUserListQuery query;
    private final BaseChannel.ChannelType channelType;
    private final String channelUrl;

    public BannedUserListQuery(@NonNull BaseChannel.ChannelType channelType, @NonNull String channelUrl) {
        this.channelType = channelType;
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<User> handler) {
        this.query = com.sendbird.android.BannedUserListQuery.create(channelType, channelUrl);
        this.query.setLimit(30);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<User> handler) {
        this.query.next(handler::onResult);
    }

    @Override
    public boolean hasMore() {
        return this.query.hasNext();
    }
}
