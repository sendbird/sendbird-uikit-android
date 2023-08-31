package com.sendbird.uikit_messaging_android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * This provides methods to manage preferences data.
 */
@SuppressLint("StaticFieldLeak")
object PreferenceUtils {
    private const val PREFERENCE_KEY_APP_ID = "PREFERENCE_KEY_APP_ID"
    private const val PREFERENCE_KEY_USER_ID = "PREFERENCE_KEY_USER_ID"
    private const val PREFERENCE_KEY_NICKNAME = "PREFERENCE_KEY_NICKNAME"
    private const val PREFERENCE_KEY_PROFILE_URL = "PREFERENCE_KEY_PROFILE_URL"
    private const val PREFERENCE_KEY_USE_DARK_THEME = "PREFERENCE_KEY_USE_DARK_THEME"
    private const val PREFERENCE_KEY_DO_NOT_DISTURB = "PREFERENCE_KEY_DO_NOT_DISTURB"

    private lateinit var context: Context
    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("sendbird", Context.MODE_PRIVATE)
    var appId: String
        get() = sharedPreferences.getString(PREFERENCE_KEY_APP_ID, "") ?: ""
        @SuppressLint("ApplySharedPref")
        set(appId) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_APP_ID, appId).commit()
        }

    var userId: String
        get() {
            val value = sharedPreferences.getString(PREFERENCE_KEY_USER_ID, "")
            return value ?: ""
        }
        set(userId) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_USER_ID, userId).apply()
        }

    var nickname: String
        get() {
            val value = sharedPreferences.getString(PREFERENCE_KEY_NICKNAME, "")
            return value ?: ""
        }
        set(nickname) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply()
        }

    var profileUrl: String
        get() {
            val value = sharedPreferences.getString(PREFERENCE_KEY_PROFILE_URL, "")
            return value ?: ""
        }
        set(profileUrl) {
            val editor = sharedPreferences.edit()
            editor.putString(PREFERENCE_KEY_PROFILE_URL, profileUrl).apply()
        }

    var isUsingDarkTheme: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_USE_DARK_THEME, false)
        set(useDarkTheme) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_USE_DARK_THEME, useDarkTheme).apply()
        }

    var doNotDisturb: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_KEY_DO_NOT_DISTURB, false)
        set(doNotDisturb) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(PREFERENCE_KEY_DO_NOT_DISTURB, doNotDisturb).apply()
        }

    fun clearAll() {
        val editor = sharedPreferences.edit()
        editor.clear().apply()
    }
}
