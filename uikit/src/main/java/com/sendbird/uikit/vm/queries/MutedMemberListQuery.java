package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.params.MemberListQueryParams;
import com.sendbird.android.user.Member;
import com.sendbird.android.user.query.MemberListQuery;
import com.sendbird.android.user.query.MutedMemberFilter;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;

public class MutedMemberListQuery implements PagedQueryHandler<Member> {
    @NonNull
    private final String channelUrl;
    @Nullable
    private MemberListQuery query;

    public MutedMemberListQuery(@NonNull String channelUrl) {
        this.channelUrl = channelUrl;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<Member> handler) {
        MemberListQueryParams memberListQueryParams = new MemberListQueryParams();
        memberListQueryParams.setLimit(30);
        memberListQueryParams.setMutedMemberFilter(MutedMemberFilter.MUTED);
        this.query = GroupChannel.createMemberListQuery(channelUrl, memberListQueryParams);
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
            return this.query.getHasNext();
        } else {
            return false;
        }
    }
}
