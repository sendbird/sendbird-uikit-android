package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.params.ApplicationUserListQueryParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultUserListQuery implements PagedQueryHandler<UserInfo> {
    @Nullable
    private com.sendbird.android.user.query.ApplicationUserListQuery query;
    private final boolean exceptMe;

    public DefaultUserListQuery() {
        this(true);
    }

    public DefaultUserListQuery(boolean exceptMe) {
        this.exceptMe = exceptMe;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<UserInfo> handler) {
        ApplicationUserListQueryParams params = new ApplicationUserListQueryParams();
        params.setLimit(30);
        this.query = SendbirdChat.createApplicationUserListQuery(params);
        loadMore(handler);
    }

    @Override
    public void loadMore(@NonNull OnListResultHandler<UserInfo> handler) {
        if (query != null) {
            this.query.next((queryResult, e) -> {
                List<UserInfo> userInfoList = null;
                if (queryResult != null) {
                    userInfoList = toUserInfoList(queryResult);
                }
                handler.onResult(userInfoList, e);
            });
        }
    }

    @Override
    public boolean hasMore() {
        if (query != null) {
            return this.query.getHasNext();
        } else {
            return false;
        }
    }

    private List<UserInfo> toUserInfoList(@NonNull List<? extends User> users) {
        final List<UserInfo> userInfoList = new ArrayList<>();
        for (User user : users) {
            if (this.exceptMe) {
                final String userId = SendbirdUIKit.getAdapter().getUserInfo().getUserId();
                if (userId.equals(user.getUserId())) {
                    continue;
                }
            }
            userInfoList.add(UserUtils.toUserInfo(user));
        }

        return userInfoList;
    }
}
