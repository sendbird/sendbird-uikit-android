package com.sendbird.uikit.utils;

import androidx.annotation.NonNull;

public class TextUtils {

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    @NonNull
    public static String capitalize(@NonNull String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
