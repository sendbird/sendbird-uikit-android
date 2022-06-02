package com.sendbird.uikit.customsample.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.User;
import com.sendbird.uikit.interfaces.UserInfo;

/**
 * Model class for a user data to adapt UIKit UserInfo interface.
 */
public class CustomUser implements UserInfo {
    User user;

    public CustomUser(@NonNull User user) {
        this.user = user;
    }

    @NonNull
    @Override
    public String getUserId() {
        return user.getUserId();
    }

    @NonNull
    @Override
    public String getNickname() {
        return user.getNickname();
    }

    @Nullable
    @Override
    public String getProfileUrl() {
        return user.getProfileUrl();
    }
}
