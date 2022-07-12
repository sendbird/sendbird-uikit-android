package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.params.ParticipantListQueryParams;
import com.sendbird.android.user.User;
import com.sendbird.android.user.query.ParticipantListQuery;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

import java.util.ArrayList;

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
        this.query = SendbirdChat.createParticipantListQuery(new ParticipantListQueryParams(channelUrl, 30));
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<User> handler) {
        if (query != null) {
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
