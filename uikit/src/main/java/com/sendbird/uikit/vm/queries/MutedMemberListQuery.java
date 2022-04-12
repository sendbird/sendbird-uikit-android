package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.GroupChannelMemberListQuery;
import com.sendbird.android.Member;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

public class MutedMemberListQuery implements PagedQueryHandler<Member> {
    @NonNull
    private final String channelUrl;
    @Nullable
    private GroupChannelMemberListQuery query;

    public MutedMemberListQuery(@NonNull String channelUrl) {
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<Member> handler) {
        this.query = GroupChannelMemberListQuery.create(channelUrl);
        this.query.setLimit(30);
        this.query.setMutedMemberFilter(GroupChannelMemberListQuery.MutedMemberFilter.MUTED);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<Member> handler) {
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
