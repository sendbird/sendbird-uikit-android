package com.sendbird.uikit.vm.queries;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.PagedQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultUserListQuery implements PagedQueryHandler<UserInfo> {
    @Nullable
    private com.sendbird.android.ApplicationUserListQuery query;
    private final boolean exceptMe;

    public DefaultUserListQuery() {
        this(true);
    }

    public DefaultUserListQuery(boolean exceptMe) {
        this.exceptMe = exceptMe;
    }

    @Override
    public void loadInitial(@NonNull OnListResultHandler<UserInfo> handler) {
        this.query = SendBird.createApplicationUserListQuery();
        this.query.setLimit(30);
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
            return this.query.hasNext();
        } else {
            return false;
        }
    }

    private List<UserInfo> toUserInfoList(@NonNull List<User> users) {
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
