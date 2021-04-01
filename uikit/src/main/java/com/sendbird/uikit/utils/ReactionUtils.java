package com.sendbird.uikit.utils;

import androidx.annotation.Nullable;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;

public class ReactionUtils {
    public static boolean useReaction(@Nullable BaseChannel channel) {
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            if (groupChannel.isSuper() || groupChannel.isBroadcast()) {
                return false;
            } else {
                return Available.isSupportReaction();
            }
        } else {
            return false;
        }
    }

    public static boolean canSendReaction(@Nullable BaseChannel channel) {
        boolean useReaction = useReaction(channel);
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            boolean isOperator = groupChannel.getMyRole() == Member.Role.OPERATOR;
            boolean isFrozen = groupChannel.isFrozen();
            return useReaction && (isOperator || !isFrozen);
        }
        return false;
    }
}
