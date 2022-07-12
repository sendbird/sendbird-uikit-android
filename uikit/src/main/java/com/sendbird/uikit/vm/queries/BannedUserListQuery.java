package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.params.BannedUserListQueryParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

import java.util.ArrayList;

public class BannedUserListQuery implements PagedQueryHandler<User> {
    @Nullable
    private com.sendbird.android.user.query.BannedUserListQuery query;
    @NonNull
    private final ChannelType channelType;
    @NonNull
    private final String channelUrl;

    public BannedUserListQuery(@NonNull ChannelType channelType, @NonNull String channelUrl) {
        this.channelType = channelType;
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<User> handler) {
        BannedUserListQueryParams params = new BannedUserListQueryParams(channelType, channelUrl);
        params.setLimit(30);
        this.query = SendbirdChat.createBannedUserListQuery(params);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<User> handler) {
        if (this.query == null) {
            handler.onResult(null, new SendbirdException("loadInitial must be called first."));
            return;
        }
        this.query.next((list, e) -> handler.onResult(list != null ? new ArrayList<>(list) : null, e));
    }

    @Override
    public boolean hasMore() {
        return this.query != null && this.query.getHasNext();
    }
}
