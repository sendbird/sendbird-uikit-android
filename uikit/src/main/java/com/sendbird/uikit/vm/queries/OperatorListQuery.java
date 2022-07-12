package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.params.OperatorListQueryParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

import java.util.ArrayList;

public class OperatorListQuery implements PagedQueryHandler<User> {
    @NonNull
    private final ChannelType channelType;
    @Nullable
    private com.sendbird.android.user.query.OperatorListQuery query;
    @NonNull
    private final String channelUrl;

    public OperatorListQuery(@NonNull ChannelType channelType, @NonNull String channelUrl) {
        this.channelType = channelType;
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<User> handler) {
        final OperatorListQueryParams params = new OperatorListQueryParams(channelType, channelUrl);
        params.setLimit(30);
        this.query = SendbirdChat.createOperatorListQuery(params);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<User> handler) {
        if (this.query != null) {
            this.query.next((list, e) -> handler.onResult(list != null ? new ArrayList<>(list) : null, e));
        }
    }

    @Override
    public boolean hasMore() {
        if (this.query != null) {
            return this.query.getHasNext();
        } else {
            return false;
        }
    }
}
