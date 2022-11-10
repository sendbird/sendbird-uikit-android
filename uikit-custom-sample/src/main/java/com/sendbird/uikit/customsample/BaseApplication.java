package com.sendbird.uikit.customsample;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDexApplication;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdError;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.InitResultHandler;
import com.sendbird.android.params.ApplicationUserListQueryParams;
import com.sendbird.android.params.FileMessageCreateParams;
import com.sendbird.android.params.GroupChannelCreateParams;
import com.sendbird.android.params.GroupChannelUpdateParams;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.android.params.OpenChannelUpdateParams;
import com.sendbird.android.params.UserMessageCreateParams;
import com.sendbird.android.params.UserMessageUpdateParams;
import com.sendbird.android.user.User;
import com.sendbird.android.user.query.ApplicationUserListQuery;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.ThreadReplySelectType;
import com.sendbird.uikit.customsample.consts.InitState;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit.customsample.models.CustomUser;
import com.sendbird.uikit.customsample.utils.PreferenceUtils;
import com.sendbird.uikit.customsample.utils.PushUtils;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.OnListResultHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.UserMentionConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Base application to initialize Sendbird UIKit.
 */
public class BaseApplication extends MultiDexApplication {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D";
    private static final MutableLiveData<InitState> initState = new MutableLiveData<>();

    /**
     * Initializes Sendbird UIKit
     */
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendbirdUIKit.init(new SendbirdUIKitAdapter() {
            @NonNull
            @Override
            public String getAppId() {
                return APP_ID;
            }

            @Override
            public String getAccessToken() {
                return "";
            }

            @NonNull
            @Override
            public UserInfo getUserInfo() {
                return new UserInfo() {
                    @NonNull
                    @Override
                    public String getUserId() {
                        return PreferenceUtils.getUserId();
                    }

                    @Override
                    public String getNickname() {
                        return PreferenceUtils.getNickname();
                    }

                    @Override
                    public String getProfileUrl() {
                        return PreferenceUtils.getProfileUrl();
                    }
                };
            }

            @NonNull
            @Override
            public InitResultHandler getInitResultHandler() {
                return new InitResultHandler() {
                    @Override
                    public void onMigrationStarted() {
                        initState.setValue(InitState.MIGRATING);
                    }

                    @Override
                    public void onInitFailed(@NonNull SendbirdException e) {
                        initState.setValue(InitState.FAILED);
                    }

                    @Override
                    public void onInitSucceed() {
                        // register push notification
                        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
                        SendbirdUIKit.setDefaultThemeMode(SendbirdUIKit.ThemeMode.Light);
                        // set logger
                        SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL);
                        // set whether to use user profile
                        SendbirdUIKit.setUseDefaultUserProfile(false);
                        // set reply type
                        SendbirdUIKit.setReplyType(ReplyType.THREAD);
                        SendbirdUIKit.setThreadReplySelectType(ThreadReplySelectType.THREAD);
                        // set custom user list query
                        SendbirdUIKit.setCustomUserListQueryHandler(getCustomUserListQuery());
                        initState.setValue(InitState.SUCCEED);
                    }
                };
            }
        }, this);

        // set custom params
        SendbirdUIKit.setCustomParamsHandler(new CustomParamsHandler() {
            @Override
            public void onBeforeCreateGroupChannel(@NonNull GroupChannelCreateParams groupChannelParams) {
                // You can set GroupChannelParams globally before creating a channel.
            }

            @Override
            public void onBeforeUpdateGroupChannel(@NonNull GroupChannelUpdateParams groupChannelParams) {
                // You can set GroupChannelParams globally before updating a channel.
            }

            @Override
            public void onBeforeSendUserMessage(@NonNull UserMessageCreateParams userMessageParams) {
                // You can set UserMessageParams globally before sending a text message.
            }

            @Override
            public void onBeforeSendFileMessage(@NonNull FileMessageCreateParams fileMessageParams) {
                // You can set FileMessageParams globally before sending a binary file message.
            }

            @Override
            public void onBeforeUpdateUserMessage(@NonNull UserMessageUpdateParams userMessageParams) {
                // You can set UserMessageParams globally before updating a text message.
            }

            @Override
            public void onBeforeUpdateOpenChannel(@NonNull OpenChannelUpdateParams openChannelParams) {
                // You can set OpenChannelParams globally before updating a channel.
            }

            @Override
            public void onBeforeCreateOpenChannel(@NonNull OpenChannelCreateParams params) {
                // You can set OpenChannelCreateParams globally before creating a open channel.
                params.setCustomType(StringSet.SB_COMMUNITY_TYPE);
            }
        });

        // set custom UIKit fragment factory
        SendbirdUIKit.setUIKitFragmentFactory(new CustomFragmentFactory());
        // set whether to use user mention
        SendbirdUIKit.setUseUserMention(true);
        // set the mention configuration
        SendbirdUIKit.setMentionConfig(new UserMentionConfig.Builder()
                .setMaxMentionCount(5)
                .setMaxSuggestionCount(10)
                .build());
    }

    /**
     * Returns the state of the result from initialization of Sendbird UIKit.
     *
     * @return the {@link InitState} instance
     */
    @NonNull
    public static LiveData<InitState> initStateChanges() {
        return initState;
    }

    /**
     * Returns the user list query to be used to retrieve user list.
     *
     * @return the {@link CustomUserListQueryHandler} instance
     */
    @NonNull
    public static CustomUserListQueryHandler getCustomUserListQuery() {
        return new CustomUserListQueryHandler() {
            @Nullable
            ApplicationUserListQuery userListQuery = null;
            @Override
            public void loadInitial(@NonNull OnListResultHandler<UserInfo> handler) {
                final ApplicationUserListQueryParams params = new ApplicationUserListQueryParams();
                params.setLimit(3);
                userListQuery = SendbirdChat.createApplicationUserListQuery(params);
                userListQuery.next((list, e) -> {
                    if (e != null || list == null) {
                        if (e != null && e.getCode() == SendbirdError.ERR_NON_AUTHORIZED) {
                            Logger.w("An error occurred because you don't have access to the user list in your application. " +
                                    "In order to gain access, you can turn on this attribute in the Access Control List settings on Sendbird Dashboard.");
                        }
                        return;
                    }

                    final List<UserInfo> customUserList = new ArrayList<>();
                    for (User user : list) {
                        customUserList.add(new CustomUser(user));
                    }
                    handler.onResult(customUserList, null);
                });
            }

            @Override
            public void loadMore(@NonNull OnListResultHandler<UserInfo> handler) {
                if (userListQuery == null) return;
                userListQuery.next((list, e) -> {
                    if (e != null || list == null) {
                        return;
                    }

                    List<UserInfo> customUserList = new ArrayList<>();
                    for (User user : list) {
                        customUserList.add(new CustomUser(user));
                    }
                    handler.onResult(customUserList, null);
                });
            }

            @Override
            public boolean hasMore() {
                if (userListQuery == null) return false;
                return userListQuery.getHasNext();
            }
        };
    }
}
