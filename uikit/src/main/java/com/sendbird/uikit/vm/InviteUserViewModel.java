package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.vm.queries.DefaultUserListQuery;

/**
 * ViewModel preparing and managing data related with the list of users when inviting users
 *
 * @since 3.0.0
 */
public class InviteUserViewModel extends UserViewModel<UserInfo> {
    /**
     * Constructor
     *
     * @param channelUrl The URL of a channel this view model is currently associated with
     * @param queryHandler A callback to be invoked when a list of data is loaded.
     * @since 3.0.0
     */
    public InviteUserViewModel(@NonNull String channelUrl, @Nullable PagedQueryHandler<UserInfo> queryHandler) {
        super(channelUrl, queryHandler);
    }

    /**
     * Creates invitable user list query.
     *
     * @return {@code PagedQueryHandler<UserInfo>} to retrieve the list of users who can be invited
     * @since 3.0.0
     */
    @NonNull
    @Override
    protected PagedQueryHandler<UserInfo> createQueryHandler(@Nullable String channelUrl) {
        final PagedQueryHandler<UserInfo> customHandler = SendbirdUIKit.getCustomUserListQueryHandler();
        return customHandler != null ? customHandler : new DefaultUserListQuery();
    }
}
