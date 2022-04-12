package com.sendbird.uikit.activities.adapter;

import androidx.annotation.NonNull;

import com.sendbird.uikit.interfaces.UserInfo;

/**
 * Adapters provides a binding from a {@link UserInfo} type to views that are displayed within a RecyclerView.
 *
 * @since 3.0.0
 */
public class UserInfoListAdapter extends SelectUserListAdapter<UserInfo> {

    @Override
    protected boolean isDisabled(@NonNull UserInfo item) {
        return disabledUserList.contains(item.getUserId());
    }

    @Override
    protected boolean isSelected(@NonNull UserInfo item) {
        return selectedUserIdList.contains(item.getUserId());
    }

    @NonNull
    @Override
    protected UserInfo toUserInfo(@NonNull UserInfo userInfo) {
        return userInfo;
    }
}
