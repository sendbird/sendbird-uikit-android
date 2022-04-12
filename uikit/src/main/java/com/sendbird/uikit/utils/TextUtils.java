package com.sendbird.uikit.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class TextUtils {

    public static boolean isEmpty(@Nullable CharSequence text) {
        return text == null || text.length() == 0;
    }

    public static boolean isNotEmpty(@Nullable CharSequence text) {
        return !isEmpty(text);
    }

    @NonNull
    public static String capitalize(@NonNull String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
