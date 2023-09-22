package com.sendbird.uikit_messaging_android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.OpenChannelCreateParams
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.consts.ThreadReplySelectType
import com.sendbird.uikit.interfaces.CustomParamsHandler
import com.sendbird.uikit.interfaces.UserInfo
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.configurations.UIKitConfig
import com.sendbird.uikit_messaging_android.consts.InitState
import com.sendbird.uikit_messaging_android.consts.StringSet
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
import com.sendbird.uikit_messaging_android.utils.PushUtils

/**
 * Base application to initialize Sendbird UIKit.
 */
class BaseApplication : MultiDexApplication() {

    companion object {
        private const val APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D"
        private val initState = MutableLiveData<InitState>()

        /**
         * Returns the state of the result from initialization of Sendbird UIKit.
         *
         * @return the [InitState] instance
         */
        fun initStateChanges(): LiveData<InitState> {
            return initState
        }
    }

    /**
     * Initializes Sendbird UIKit
     */
    override fun onCreate() {
        super.onCreate()
        PreferenceUtils.init(applicationContext)
        SendbirdUIKit.init(object : SendbirdUIKitAdapter {
            override fun getAppId(): String {
                var appId: String
                if (BuildConfig.DEBUG) {
                    appId = PreferenceUtils.appId
                    if (appId.isEmpty()) {
                        appId = APP_ID
                    }
                } else {
                    appId = APP_ID
                }
                Logger.d("++ app id : %s", appId)
                return appId
            }

            override fun getAccessToken(): String {
                return ""
            }

            override fun getUserInfo(): UserInfo {
                return object : UserInfo {
                    override fun getUserId(): String {
                        return PreferenceUtils.userId
                    }

                    override fun getNickname(): String {
                        return PreferenceUtils.nickname
                    }

                    override fun getProfileUrl(): String {
                        return PreferenceUtils.profileUrl
                    }
                }
            }

            override fun getInitResultHandler(): InitResultHandler {
                return object : InitResultHandler {
                    override fun onMigrationStarted() {
                        initState.value = InitState.MIGRATING
                    }

                    override fun onInitFailed(e: SendbirdException) {
                        initState.value = InitState.FAILED
                    }

                    override fun onInitSucceed() {
                        initState.value = InitState.SUCCEED
                    }
                }
            }
        }, this)
        val useDarkTheme = PreferenceUtils.isUsingDarkTheme
        SendbirdUIKit.setDefaultThemeMode(if (useDarkTheme) SendbirdUIKit.ThemeMode.Dark else SendbirdUIKit.ThemeMode.Light)
        // register push notification
        PushUtils.registerPushHandler(MyFirebaseMessagingService())
        // set logger
        SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL)
        // set whether to use user profile
        UIKitConfig.common.enableUsingDefaultUserProfile = true
        // set whether to use typing indicators in channel list
        UIKitConfig.groupChannelListConfig.enableTypingIndicator = true
        // set whether to use read/delivery receipt in channel list
        UIKitConfig.groupChannelListConfig.enableMessageReceiptStatus = true
        // set whether to use user mention
        UIKitConfig.groupChannelConfig.enableMention = true
        // set reply type
        UIKitConfig.groupChannelConfig.replyType = ReplyType.THREAD
        UIKitConfig.groupChannelConfig.threadReplySelectType = ThreadReplySelectType.THREAD
        // set whether to use voice message
        UIKitConfig.groupChannelConfig.enableVoiceMessage = true
        // set whether to use Multiple Files Message
        UIKitConfig.groupChannelConfig.enableMultipleFilesMessage = true

        // set custom params
        SendbirdUIKit.setCustomParamsHandler(object : CustomParamsHandler {
            override fun onBeforeCreateOpenChannel(params: OpenChannelCreateParams) {
                // You can set OpenChannelCreateParams globally before creating a open channel.
                params.customType = StringSet.SB_COMMUNITY_TYPE
            }
        })
    }
}
