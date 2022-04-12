package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.Member;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.vm.queries.ChannelMemberListQuery;

/**
 * ViewModel preparing and managing data related with the list of members who can be promoted
 *
 * @since 3.0.0
 */
public class PromoteOperatorViewModel extends UserViewModel<Member> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * @since 3.0.0
     */
    public PromoteOperatorViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<Member> queryHandler) {
        super(channelUrl, queryHandler);
    }

    /**
     * Creates promote operator list query.
     *
     * @param channelUrl The url of {@code GroupChannel} with members to be fetched by the query
     * @return {@code PagedQueryHandler<Member>} to retrieve the list of members to be operator
     * @since 3.0.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<Member> createQueryHandler(@NonNull String channelUrl) {
        return new ChannelMemberListQuery(channelUrl);
    }
}
