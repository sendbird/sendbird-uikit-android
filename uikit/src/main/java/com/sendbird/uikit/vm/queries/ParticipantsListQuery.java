package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.ParticipantListQuery;
import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

public class ParticipantsListQuery implements PagedQueryHandler<User> {
    @Nullable
    private ParticipantListQuery query;
    @NonNull
    private final String channelUrl;

    public ParticipantsListQuery(@NonNull String channelUrl) {
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<User> handler) {
        this.query = ParticipantListQuery.create(channelUrl);
        this.query.setLimit(30);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<User> handler) {
        if (query != null) {
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
