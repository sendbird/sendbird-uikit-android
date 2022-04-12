package com.sendbird.uikit.activities.adapter;

import androidx.annotation.NonNull;

import com.sendbird.android.Member;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.utils.UserUtils;

/**
 * ParticipantsListAdapter provides a binding from a {@link Member} type data set to views that are displayed within a RecyclerView.
 *
 * @since 3.0.0
 */
public class PromoteOperatorListAdapter extends SelectUserListAdapter<Member> {
    @Override
    protected boolean isDisabled(@NonNull Member item) {
        return item.getRole() == Member.Role.OPERATOR;
    }

    @Override
    protected boolean isSelected(@NonNull Member item) {
        return selectedUserIdList.contains(item.getUserId());
    }

    @NonNull
    @Override
    protected UserInfo toUserInfo(@NonNull Member member) {
        return UserUtils.toUserInfo(member);
    }
}
