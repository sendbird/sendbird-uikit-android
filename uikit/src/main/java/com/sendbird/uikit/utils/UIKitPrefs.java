package com.sendbird.uikit.utils;

import android.content.Context;
import android.content.SharedPreferences;

final public class UIKitPrefs {
    private static final String PREFERENCE_FILE_NAME = "com.sendbird.uikit.local_preference";

    private static SharedPreferences preferences;
    private UIKitPrefs() {}

    public static void init(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(
                PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE
        );
    }

    public static void clearAll() {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
    }

    public static void remove(String key ) {
        if (preferences == null) return;
        if (preferences.contains(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(key).apply();
        }
    }

    public static void putString(String key, String value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value).apply();
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defValue) {
        if (preferences == null) return defValue;
        return preferences.getString(key, defValue);
    }

    public static void putLong(String key, long value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value).apply();
    }

    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    public static long getLong(String key, long defValue) {
        if (preferences == null) return defValue;
        return preferences.getLong(key, defValue);
    }

    public static void putInt(String key, int value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defValue) {
        if (preferences == null) return defValue;
        return preferences.getInt(key, defValue);
    }

    public static void putBoolean(String key, boolean value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        if (preferences == null) return defValue;
        return preferences.getBoolean(key, defValue);
    }
}
