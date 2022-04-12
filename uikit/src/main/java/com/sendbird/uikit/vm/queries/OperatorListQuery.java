package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

public class OperatorListQuery implements PagedQueryHandler<User> {
    @NonNull
    private final BaseChannel.ChannelType channelType;
    @Nullable
    private com.sendbird.android.OperatorListQuery query;
    @NonNull
    private final String channelUrl;

    public OperatorListQuery(@NonNull BaseChannel.ChannelType channelType, @NonNull String channelUrl) {
        this.channelType = channelType;
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<User> handler) {
        this.query = com.sendbird.android.OperatorListQuery.create(channelType, channelUrl);
        this.query.setLimit(30);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<User> handler) {
        if (this.query != null) {
            this.query.next(handler::onResult);
        }
    }

    @Override
    public boolean hasMore() {
        if (this.query != null) {
            return this.query.hasNext();
        } else {
            return false;
        }
    }
}
