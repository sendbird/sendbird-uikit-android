package com.sendbird.uikit.samples

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.OpenChannelCreateParams
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.consts.ThreadReplySelectType
import com.sendbird.uikit.consts.TypingIndicatorType
import com.sendbird.uikit.interfaces.CustomParamsHandler
import com.sendbird.uikit.interfaces.UserInfo
import com.sendbird.uikit.model.configurations.UIKitConfig
import com.sendbird.uikit.samples.common.consts.InitState
import com.sendbird.uikit.samples.common.consts.SampleType
import com.sendbird.uikit.samples.common.consts.StringSet
import com.sendbird.uikit.samples.common.fcm.MyFirebaseMessagingService
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils

private const val APP_ID = "FEA2129A-EA73-4EB9-9E0B-EC738E7EB768"
internal const val enableAiChatBotSample = true
internal const val enableNotificationSample = true

class BaseApplication : MultiDexApplication() {
    companion object {
        internal val initState = MutableLiveData(InitState.NONE)

        internal val adapter by lazy {
            object : SendbirdUIKitAdapter {
                override fun getAppId(): String = PreferenceUtils.appId.ifEmpty { APP_ID }

                override fun getAccessToken(): String? = null

                override fun getUserInfo(): UserInfo = object : UserInfo {
                    override fun getUserId(): String = PreferenceUtils.userId
                    override fun getNickname(): String = PreferenceUtils.nickname
                    override fun getProfileUrl(): String = PreferenceUtils.profileUrl
                }

                override fun getInitResultHandler(): InitResultHandler = object : InitResultHandler {
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
        }

        /**
         * Returns the state of the result from initialization of Sendbird UIKit.
         *
         * @return the [InitState] instance
         */
        fun initStateChanges(): LiveData<InitState> {
            return initState
        }

        private fun initUIKit(context: Context) {
            SendbirdUIKit.init(adapter, context)

            // set theme mode
            SendbirdUIKit.setDefaultThemeMode(PreferenceUtils.themeMode)
            // register push notification
            SendbirdPushHelper.registerHandler(MyFirebaseMessagingService())
            // set logger
            SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL)
        }

        /**
         * In a sample app, different contextual settings are used in a single app.
         * These are only used in the sample, because if the app kills and resurrects due to low memory, the last used sample settings should be preserved.
         */
        fun setupConfigurations() {
            when (PreferenceUtils.selectedSampleType) {
                SampleType.Basic -> {
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
                    // set typing indicator types
                    UIKitConfig.groupChannelConfig.typingIndicatorTypes = setOf(TypingIndicatorType.BUBBLE, TypingIndicatorType.TEXT)
                    // set whether to use feedback
                    UIKitConfig.groupChannelConfig.enableFeedback = true

                    // set custom params
                    SendbirdUIKit.setCustomParamsHandler(object : CustomParamsHandler {
                        override fun onBeforeCreateOpenChannel(params: OpenChannelCreateParams) {
                            // You can set OpenChannelCreateParams globally before creating a open channel.
                            params.customType = StringSet.SB_COMMUNITY_TYPE
                        }
                    })
                }
                SampleType.Notification -> {}
                SampleType.Customization -> {}
                SampleType.AiChatBot -> {
                    // set typing indicator types
                    UIKitConfig.groupChannelConfig.typingIndicatorTypes = setOf(TypingIndicatorType.BUBBLE)
                    // set whether to use feedback
                    UIKitConfig.groupChannelConfig.enableFeedback = true
                }
                else -> {
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        PreferenceUtils.init(applicationContext)

        // initialize SendbirdUIKit
        initUIKit(this)

        // setup uikit configurations
        setupConfigurations()
    }
}
