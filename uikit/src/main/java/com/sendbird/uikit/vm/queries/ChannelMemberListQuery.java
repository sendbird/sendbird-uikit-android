package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.params.MemberListQueryParams;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.query.MemberListQuery;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

public class ChannelMemberListQuery implements PagedQueryHandler<Member> {
    @NonNull
    private final String channelUrl;
    @Nullable
    private MemberListQuery query;

    public ChannelMemberListQuery(@NonNull String channelUrl) {
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<Member> handler) {
        MemberListQueryParams memberListQueryParams = new MemberListQueryParams();
        memberListQueryParams.setLimit(30);
        this.query = GroupChannel.createMemberListQuery(channelUrl, memberListQueryParams);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<Member> handler) {
        if (query != null) {
            query.next(handler::onResult);
        }
    }

    @Override
    public boolean hasMore() {
        if (query != null) {
            return query.getHasNext();
        } else {
            return false;
        }
    }
}
