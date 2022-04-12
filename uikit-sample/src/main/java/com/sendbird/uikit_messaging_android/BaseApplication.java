package com.sendbird.uikit_messaging_android;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.multidex.MultiDexApplication;

import com.sendbird.android.SendBirdException;
import com.sendbird.android.handlers.InitResultHandler;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit_messaging_android.consts.InitState;
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.utils.PushUtils;

public class BaseApplication extends MultiDexApplication {

    private static final String APP_ID = "2D7B4CDB-932F-4082-9B09-A1153792DC8D";
    private static final MutableLiveData<InitState> initState = new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceUtils.init(getApplicationContext());

        SendbirdUIKit.init(new SendbirdUIKitAdapter() {
            @Override
            @NonNull
            public String getAppId() {
                return APP_ID;
            }

            @Override
            @NonNull
            public String getAccessToken() {
                return "";
            }

            @Override
            @NonNull
            public UserInfo getUserInfo() {
                return new UserInfo() {
                    @NonNull
                    @Override
                    public String getUserId() {
                        return PreferenceUtils.getUserId();
                    }

                    @NonNull
                    @Override
                    public String getNickname() {
                        return PreferenceUtils.getNickname();
                    }

                    @NonNull
                    @Override
                    public String getProfileUrl() {
                        return PreferenceUtils.getProfileUrl();
                    }
                };
            }

            @Override
            @NonNull
            public InitResultHandler getInitResultHandler() {
                return new InitResultHandler() {
                    @Override
                    public void onMigrationStarted() {
                        initState.setValue(InitState.MIGRATING);
                    }

                    @Override
                    public void onInitFailed(@NonNull SendBirdException e) {
                        initState.setValue(InitState.FAILED);
                    }

                    @Override
                    public void onInitSucceed() {
                        initState.setValue(InitState.SUCCEED);
                    }
                };
            }
        }, this);

        boolean useDarkTheme = PreferenceUtils.isUsingDarkTheme();
        SendbirdUIKit.setDefaultThemeMode(useDarkTheme ? SendbirdUIKit.ThemeMode.Dark : SendbirdUIKit.ThemeMode.Light);
        PushUtils.registerPushHandler(new MyFirebaseMessagingService());
        SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL);
        SendbirdUIKit.setUseDefaultUserProfile(true);
        SendbirdUIKit.setUseChannelListTypingIndicators(true);
        SendbirdUIKit.setUseChannelListMessageReceiptStatus(true);
    }

    @NonNull
    public static LiveData<InitState> initStateChanges() {
        return initState;
    }
}
