package com.sendbird.uikit.utils;

import androidx.annotation.NonNull;

import com.sendbird.android.User;
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
}
