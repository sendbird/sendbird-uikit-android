package com.sendbird.uikit.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.R;
import com.sendbird.uikit.interfaces.UserInfo;

public class UserUtils {
    public static <T extends User> UserInfo toUserInfo(@NonNull T user) {
        return new UserInfo() {
            @Override
            public String getUserId() {
                return user.getUserId();
            }

            @Override
            public String getNickname() {
                return user.getNickname();
            }

            @Override
            public String getProfileUrl() {
                return user.getProfileUrl();
            }
        };
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, @Nullable User user) {
        String nickname = context.getString(R.string.sb_text_channel_list_title_unknown);
        if (user == null) return nickname;

        if (user.getUserId() != null && user.getUserId().equals(SendBird.getCurrentUser().getUserId())) {
            nickname = context.getString(R.string.sb_text_you);
        } else if (!TextUtils.isEmpty(user.getNickname())) {
            nickname = user.getNickname();
        }
        return nickname;
    }
}
