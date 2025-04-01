package com.sendbird.uikit;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.AppInfo;
import com.sendbird.android.NotificationInfo;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.exception.SendbirdConnectionRequiredException;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.AuthenticationHandler;
import com.sendbird.android.handler.CompletionHandler;
import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.android.handler.DisconnectHandler;
import com.sendbird.android.handler.InitResultHandler;
import com.sendbird.android.internal.sb.SendbirdPlatform;
import com.sendbird.android.internal.sb.SendbirdProduct;
import com.sendbird.android.internal.sb.SendbirdSdkInfo;
import com.sendbird.android.params.GroupChannelCreateParams;
import com.sendbird.android.params.InitParams;
import com.sendbird.android.params.UserUpdateParams;
import com.sendbird.android.template.MessageTemplateInfo;
import com.sendbird.android.user.User;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.consts.ThreadReplySelectType;
import com.sendbird.uikit.fragments.UIKitFragmentFactory;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.internal.contracts.SendbirdChatContract;
import com.sendbird.uikit.internal.contracts.SendbirdChatImpl;
import com.sendbird.uikit.internal.contracts.TaskQueueContract;
import com.sendbird.uikit.internal.contracts.TaskQueueImpl;
import com.sendbird.uikit.internal.singleton.MessageDisplayDataManager;
import com.sendbird.uikit.internal.singleton.MessageTemplateManager;
import com.sendbird.uikit.internal.singleton.NotificationChannelManager;
import com.sendbird.uikit.internal.singleton.UIKitConfigRepository;
import com.sendbird.uikit.internal.tasks.JobResultTask;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.EdgeToEdgeConfig;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.model.UserMentionConfig;
import com.sendbird.uikit.model.VoiceRecorderConfig;
import com.sendbird.uikit.model.configurations.Common;
import com.sendbird.uikit.model.configurations.UIKitConfig;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.utils.UIKitPrefs;

import org.jetbrains.annotations.TestOnly;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sendbird UIKit Main Class.
 */
public class SendbirdUIKit {
    private static volatile SendbirdUIKitAdapter adapter;

    /**
     * UIKit log level. It depends on android Log level.
     */
    public enum LogLevel {
        ALL(Log.VERBOSE), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR), NONE(Integer.MAX_VALUE);

        final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * UIKit theme mode.
     */
    public enum ThemeMode {
        /**
         * Light mode.
         */
        Light(R.style.AppTheme_Sendbird, R.color.primary_main, R.color.secondary_main, R.color.onlight_text_low_emphasis, R.color.error_main),
        /**
         * Dark mode.
         */
        Dark(R.style.AppTheme_Dark_Sendbird, R.color.primary_light, R.color.secondary_light, R.color.ondark_text_low_emphasis, R.color.error_light);

        @StyleRes
        final int resId;
        @ColorRes
        final int primaryTintColorResId;
        @ColorRes
        final int secondaryTintColorResId;
        @ColorRes
        final int monoTintColorResId;
        @ColorRes
        final int errorColorResId;

        ThemeMode(@StyleRes int resId, @ColorRes int primaryTintColorResId, @ColorRes int secondaryTintColorResId, @ColorRes int monoTintColorResId, @ColorRes int errorColorResId) {
            this.resId = resId;
            this.primaryTintColorResId = primaryTintColorResId;
            this.secondaryTintColorResId = secondaryTintColorResId;
            this.monoTintColorResId = monoTintColorResId;
            this.errorColorResId = errorColorResId;
        }

        /**
         * Returns the style resource id for the current theme.
         *
         * @return The style resource id for the current theme.
         */
        @StyleRes
        public int getResId() {
            return resId;
        }

        /**
         * Returns the resource id of the primary tint color for the current theme.
         *
         * @return The resource id of the primary tint color for the current theme.
         */
        @ColorRes
        public int getPrimaryTintResId() {
            return primaryTintColorResId;
        }

        /**
         * Returns the resource id of the secondary tint color for the current theme.
         *
         * @return The resource id of the secondary tint color for the current theme.
         */
        @ColorRes
        public int getSecondaryTintResId() {
            return secondaryTintColorResId;
        }

        /**
         * Returns the resource id of the mono tint color for the current theme.
         *
         * @return The resource id of the mono tint color for the current theme.
         */
        @ColorRes
        public int getMonoTintResId() {
            return monoTintColorResId;
        }

        /**
         * Returns the resource id of the error tint color for the current theme.
         *
         * @return The resource id of the error tint color for the current theme.
         */
        @ColorRes
        public int getErrorColorResId() {
            return errorColorResId;
        }

        /**
         * Returns the {@code ColorStateList} of the primary tint color for the current theme.
         *
         * @return {@code ColorStateList} of the primary tint color for the current theme.
         */
        @NonNull
        public ColorStateList getPrimaryTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, primaryTintColorResId);
        }

        /**
         * Returns the {@code ColorStateList} of the secondary tint color for the current theme.
         *
         * @return {@code ColorStateList} of the secondary tint color for the current theme.
         */
        @NonNull
        public ColorStateList getSecondaryTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, secondaryTintColorResId);
        }

        /**
         * Returns the {@code ColorStateList} of the mono tint color for the current theme.
         *
         * @return {@code ColorStateList} of the mono tint color for the current theme.
         */
        @NonNull
        public ColorStateList getMonoTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, monoTintColorResId);
        }

        /**
         * Returns the {@code ColorStateList} of the error tint color for the current theme.
         *
         * @return {@code ColorStateList} of the error tint color for the current theme.
         */
        @NonNull
        public ColorStateList getErrorTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, errorColorResId);
        }
    }

    private static final int DEFAULT_RESIZING_WIDTH_SIZE = 1080;
    private static final int DEFAULT_RESIZING_HEIGHT_SIZE = 1920;

    @Nullable
    private static UIKitConfigRepository uikitConfigRepo;
    @NonNull
    private static volatile ThemeMode defaultThemeMode = ThemeMode.Light;
    private static volatile boolean useUserIdForNickname = false;
    private static volatile boolean useCompression = true;
    @Nullable
    private static CustomUserListQueryHandler customUserListQueryHandler;
    @Nullable
    private static CustomParamsHandler customParamsHandler;
    private static int compressQuality = 70;
    @NonNull
    private static Pair<Integer, Integer> resizingSize = new Pair<>(DEFAULT_RESIZING_WIDTH_SIZE, DEFAULT_RESIZING_HEIGHT_SIZE);
    @NonNull
    private static UIKitFragmentFactory fragmentFactory = new UIKitFragmentFactory();
    @NonNull
    private static UserMentionConfig userMentionConfig = new UserMentionConfig.Builder().build();
    @NonNull
    private static VoiceRecorderConfig voiceRecorderConfig = new VoiceRecorderConfig();
    @NonNull
    private static EdgeToEdgeConfig edgeToEdgeConfig = new EdgeToEdgeConfig();

    static void clearAll() {
        SendbirdUIKit.customUserListQueryHandler = null;
        defaultThemeMode = ThemeMode.Light;
        UIKitPrefs.clearAll();
        NotificationChannelManager.clearAll();
        MessageDisplayDataManager.clearAll();
        MessageTemplateManager.clearAll();
    }

    /**
     * Initializes Sendbird with given app ID.
     *
     * @param adapter The {@link SendbirdUIKitAdapter} providing an app ID, a information of the user.
     * @param context <code>Context</code> of <code>Application</code>.
     */
    public synchronized static void init(@NonNull SendbirdUIKitAdapter adapter, @NonNull Context context) {
        init(new SendbirdChatImpl(), adapter, new UIKitConfigRepository(context, adapter.getAppId()), context, false);
    }

    /**
     * Initializes Sendbird from foreground with given app ID.
     *
     * @param adapter The {@link SendbirdUIKitAdapter} providing an app ID, a information of the user.
     * @param context <code>Context</code> of <code>Application</code>.
     * since 2.1.8
     */
    public synchronized static void initFromForeground(@NonNull SendbirdUIKitAdapter adapter, @NonNull Context context) {
        init(new SendbirdChatImpl(), adapter, new UIKitConfigRepository(context, adapter.getAppId()), context, true);
    }

    @VisibleForTesting
    synchronized static void init(
        @NonNull SendbirdChatContract sendbirdChatContract,
        @NonNull SendbirdUIKitAdapter adapter,
        @NonNull UIKitConfigRepository uikitConfigRepo,
        @NonNull Context context,
        boolean isForeground) {
        SendbirdUIKit.adapter = adapter;
        SendbirdUIKit.uikitConfigRepo = uikitConfigRepo;

        final InitResultHandler handler = adapter.getInitResultHandler();
        final InitResultHandler initResultHandler = new InitResultHandler() {
            @Override
            public void onMigrationStarted() {
                Logger.d(">> onMigrationStarted()");
                handler.onMigrationStarted();
            }

            @Override
            public void onInitFailed(@NonNull SendbirdException e) {
                Logger.d(">> onInitFailed() e=%s", e);
                Logger.e(e);
                handler.onInitFailed(e);
            }

            @Override
            public void onInitSucceed() {
                Logger.d(">> onInitSucceed()");
                try {
                    sendbirdChatContract.addExtension(StringSet.sb_uikit, BuildConfig.VERSION_NAME);
                } catch (Throwable ignored) {
                }
                try {
                    SendbirdSdkInfo o = new SendbirdSdkInfo(
                        SendbirdProduct.UIKIT_CHAT,
                        SendbirdPlatform.ANDROID,
                        BuildConfig.VERSION_NAME
                    );
                    sendbirdChatContract.addSendbirdExtensions(Collections.singletonList(o), null);
                } catch (Throwable ignored) {
                }

                handler.onInitSucceed();
            }
        };

        final com.sendbird.android.LogLevel logLevel = BuildConfig.DEBUG ? com.sendbird.android.LogLevel.VERBOSE : com.sendbird.android.LogLevel.WARN;
        // useCaching=true is required for UIKit
        final InitParams initParams = new InitParams(adapter.getAppId(), context, true, logLevel, isForeground);
        sendbirdChatContract.init(initParams, initResultHandler);
        FileUtils.removeDeletableDir(context.getApplicationContext());
        UIKitPrefs.init(context.getApplicationContext());
        NotificationChannelManager.init(context.getApplicationContext());
        MessageTemplateManager.init(context.getApplicationContext());
        EmojiManager.init();
    }

    /**
     * Returns set value whether the user profile uses.
     * since 1.2.2
     *
     * @return the value whether the user profile uses.
     * @deprecated 3.6.0
     * <p> Use {@link Common#getEnableUsingDefaultUserProfile()} instead.</p>
     */
    @Deprecated
    public static boolean shouldUseDefaultUserProfile() {
        return UIKitConfig.getCommon().getEnableUsingDefaultUserProfile();
    }

    /**
     * Sets whether the user profile uses.
     * since 1.2.2
     *
     * @param useDefaultUserProfile If <code>true</code> the default user profile included the UIKit will be shown, <code>false</code> other wise.
     * @deprecated 3.6.0
     * <p> Use {@link Common#setEnableUsingDefaultUserProfile(Boolean)} instead.</p>
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void setUseDefaultUserProfile(boolean useDefaultUserProfile) {
        UIKitConfig.getCommon().setEnableUsingDefaultUserProfile(useDefaultUserProfile);
    }

    /**
     * Sets whether the typing indicator is used on the channel list screen.
     * since 3.0.0
     *
     * @param useChannelListTypingIndicators If <code>true</code> the typing indicator will be shown at the channel list item, <code>false</code> other wise.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelListConfig#setEnableTypingIndicator(Boolean)}   instead.</p>
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void setUseChannelListTypingIndicators(boolean useChannelListTypingIndicators) {
        UIKitConfig.getGroupChannelListConfig().setEnableTypingIndicator(useChannelListTypingIndicators);
    }

    /**
     * Returns set value whether the typing indicator is used on the channel list screen.
     * since 3.0.0
     *
     * @return the value whether the typing indicator is used on the channel list screen.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelListConfig#getEnableTypingIndicator()}   instead.</p>
     */
    @Deprecated
    public static boolean isUsingChannelListTypingIndicators() {
        return UIKitConfig.getGroupChannelListConfig().getEnableTypingIndicator();
    }

    /**
     * Sets whether the states read-receipt and delivery-receipt are used on the channel list screen.
     * since 3.0.0
     *
     * @param useChannelListMessageReceiptStatus If <code>true</code> the states read-receipt and delivery-receipt will be shown at the channel list item, <code>false</code> other wise.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelListConfig#setEnableMessageReceiptStatus(Boolean)} instead.</p>
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void setUseChannelListMessageReceiptStatus(boolean useChannelListMessageReceiptStatus) {
        UIKitConfig.getGroupChannelListConfig().setEnableMessageReceiptStatus(useChannelListMessageReceiptStatus);
    }

    /**
     * Returns set value whether the states read-receipt and delivery-receipt are used on the channel list screen.
     * since 3.0.0
     *
     * @return the value whether the states read-receipt and delivery-receipt are used on the channel list screen.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelListConfig#getEnableMessageReceiptStatus()} (Boolean)} instead.</p>
     */
    @Deprecated
    public static boolean isUsingChannelListMessageReceiptStatus() {
        return UIKitConfig.getGroupChannelListConfig().getEnableMessageReceiptStatus();
    }

    /**
     * Sets whether the user mention is used on the channel screen.
     * since 3.0.0
     *
     * @param useMention If <code>true</code> the mention will be used at the channel, <code>false</code> other wise.
     * @since 3.0.0
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#setEnableMention(Boolean)} instead.</p>
     */
    @Deprecated
    public static void setUseUserMention(boolean useMention) {
        UIKitConfig.getGroupChannelConfig().setEnableMention(useMention);
    }

    /**
     * Sets whether a nickname uses a user ID when there is no user nickname based on the user ID.
     * since 3.3.0
     *
     * @param useUserIdForNickname If <code>true</code> the user's nickname uses user ID when the nickname is empty, <code>false</code> other wise.
     */
    public static void setUseUserIdForNickname(boolean useUserIdForNickname) {
        SendbirdUIKit.useUserIdForNickname = useUserIdForNickname;
    }

    /**
     * Returns set value whether the user mention is used on the channel screen.
     * since 3.0.0
     *
     * @return The value whether the user mention is used on the channel screen.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#setEnableMention(Boolean)} instead.</p>
     */
    @Deprecated
    public static boolean isUsingUserMention() {
        return UIKitConfig.getGroupChannelConfig().getEnableMention();
    }

    /**
     * Returns set value whether a nickname uses a user ID when there is no user nickname based on the user ID.
     * since 3.3.0
     *
     * @return The value whether a nickname uses a user ID when there is no user nickname based on the user ID.
     */
    public static boolean isUsingUserIdForNickname() {
        return SendbirdUIKit.useUserIdForNickname;
    }

    /**
     * Sets whether the voice message is used on the channel screen and message thread screen.
     * The voice message is only active in group channels.
     * since 3.4.0
     *
     * @param useVoiceMessage If <code>true</code> the voice message will be used, <code>false</code> other wise.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#setEnableVoiceMessage(Boolean)}   instead.</p>
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static void setUseVoiceMessage(boolean useVoiceMessage) {
        UIKitConfig.getGroupChannelConfig().setEnableVoiceMessage(useVoiceMessage);
    }

    /**
     * Returns set value whether the voice message is used on the channel screen and message thread screen.
     * The voice message is only active in group channels.
     * since 3.4.0
     *
     * @return The value whether the voice message is used on the channel screen, message thread screen.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#getEnableVoiceMessage()}.</p>
     */
    @Deprecated
    public static boolean isUsingVoiceMessage() {
        return UIKitConfig.getGroupChannelConfig().getEnableVoiceMessage();
    }

    /**
     * Sets <code>ReplyType</code>, which is how replies are displayed in the message list.
     * since 2.2.0
     *
     * @param replyType A type that represents how to display replies in message list
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#setReplyType(ReplyType)} instead.</p>
     */
    @Deprecated
    public static void setReplyType(@NonNull ReplyType replyType) {
        UIKitConfig.getGroupChannelConfig().setReplyType(replyType);
    }

    /**
     * Sets <code>ThreadReplySelectType</code>, which is how replies are displayed in the message list.
     * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
     * since 3.3.0
     *
     * @param threadReplySelectType A type that represents where to go when selecting a reply
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#setThreadReplySelectType(ThreadReplySelectType)} instead.</p>
     */
    @Deprecated
    public static void setThreadReplySelectType(@NonNull ThreadReplySelectType threadReplySelectType) {
        UIKitConfig.getGroupChannelConfig().setThreadReplySelectType(threadReplySelectType);
    }

    /**
     * Returns <code>ReplyType</code>, which is how replies are displayed in the message list.
     * since 2.2.0
     *
     * @return The value of <code>ReplyType</code>.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#getReplyType()} instead.</p>
     */
    @Deprecated
    @NonNull
    public static ReplyType getReplyType() {
        return UIKitConfig.getGroupChannelConfig().getReplyType();
    }

    /**
     * Returns <code>ThreadReplySelectType</code>, which determines where to go when reply is selected.
     * <code>ThreadReplySelectType</code> can be applied when the reply type is <code>ReplyType.THREAD</code>.
     * since 3.3.0
     *
     * @return The value of <code>ThreadReplySelectType</code>.
     * @deprecated 3.6.0
     * <p> Use {@link UIKitConfig#getGroupChannelConfig#getThreadReplySelectType()} instead.</p>
     */
    @Deprecated
    @NonNull
    public static ThreadReplySelectType getThreadReplySelectType() {
        return UIKitConfig.getGroupChannelConfig().getThreadReplySelectType();
    }

    /**
     * Connects to Sendbird with given <code>UserInfo</code>.
     *
     * @param handler Callback handler.
     */
    public static void connect(@Nullable ConnectHandler handler) {
        connectInternal(new SendbirdChatImpl(), new TaskQueueImpl(), handler);
    }

    /**
     * Authenticate to Sendbird with given <code>UserInfo</code>.
     * Unlike {@link #connect(ConnectHandler)}, it is used to issue the necessary credentials when using the API required for FeedNotification.
     *
     * @param handler Callback handler.
     * @deprecated As of 3.19.0, replaced by {@link #authenticate(AuthenticationHandler)}.
     * since 3.7.0
     */
    @Deprecated
    public static void authenticateFeed(@Nullable AuthenticationHandler handler) {
        authenticate(handler);
    }

    /**
     * Authenticate to Sendbird with given <code>UserInfo</code>.
     * Unlike {@link #connect(ConnectHandler)}, it is used to issue the necessary credentials when using the API required for FeedNotification.
     *
     * @param handler Callback handler.
     * since 3.19.0
     */
    public static void authenticate(@Nullable AuthenticationHandler handler) {
        connectInternal(ConnectType.AUTHENTICATE_FEED, new SendbirdChatImpl(), new TaskQueueImpl(), (user, e1) -> {
            if (handler != null) handler.onAuthenticated(user, e1);
        });
    }

    /**
     * This method is used only internally.
     *
     * @param apiHost Custom API Host. Internal use only.
     * @param wsHost  Custom WS Host. Internal use only.
     * since 3.19.0
     */
    public static synchronized void setCustomHosts(@Nullable String apiHost, @Nullable String wsHost) {
        UIKitPrefs.putString(StringSet.KEY_CUSTOM_API_HOST, apiHost);
        UIKitPrefs.putString(StringSet.KEY_CUSTOM_WS_HOST, wsHost);
    }

    private enum ConnectType {
        CONNECT,
        AUTHENTICATE_FEED
    }

    @VisibleForTesting
    static void connectInternal(@NonNull SendbirdChatContract sendbirdChat,
                                @NonNull TaskQueueContract taskQueueContract,
                                @Nullable ConnectHandler handler) {
        connectInternal(ConnectType.CONNECT, sendbirdChat, taskQueueContract, handler);
    }

    private static void connectInternal(
        @NonNull ConnectType connectType,
        @NonNull SendbirdChatContract sendbirdChat,
        @NonNull TaskQueueContract taskQueueContract,
        @Nullable ConnectHandler handler) {
        taskQueueContract.addTask(new JobResultTask<Pair<User, SendbirdException>>() {
            @Override
            public Pair<User, SendbirdException> call() throws Exception {
                final Pair<User, SendbirdException> data;
                if (connectType == ConnectType.AUTHENTICATE_FEED) {
                    data = authenticateFeedBlocking(sendbirdChat);
                } else {
                    data = connectBlocking(sendbirdChat);
                }

                final User user = data.first;
                final SendbirdException error = data.second;
                Logger.d("++ user=%s, error=%s", user, error);
                if (error == null && user != null) {
                    UserInfo userInfo = adapter.getUserInfo();
                    String userId = userInfo.getUserId();
                    String nickname = TextUtils.isEmpty(userInfo.getNickname()) ? user.getNickname() : userInfo.getNickname();
                    if (useUserIdForNickname && TextUtils.isEmpty(nickname)) nickname = userId;
                    String profileUrl = TextUtils.isEmpty(userInfo.getProfileUrl()) ? user.getProfileUrl() : userInfo.getProfileUrl();
                    if (!nickname.equals(user.getNickname()) || (!TextUtils.isEmpty(profileUrl) && !profileUrl.equals(user.getProfileUrl()))) {
                        final UserUpdateParams params = new UserUpdateParams();
                        params.setNickname(nickname);
                        params.setProfileImageUrl(profileUrl);
                        updateUserInfoBlocking(sendbirdChat, params);
                    }

                    Logger.dev("++ user nickname = %s, profileUrl = %s", user.getNickname(), user.getProfileUrl());

                    final AppInfo appInfo = sendbirdChat.getAppInfo();
                    if (appInfo != null) {
                        if (appInfo.getUseReaction()
                            && appInfo.needUpdateEmoji(EmojiManager.getEmojiHash())
                            && connectType == ConnectType.CONNECT) {
                            updateEmojiList();
                        }

                        fetchTemplatesBlocking(sendbirdChat);
                        if (SendbirdUIKit.uikitConfigRepo != null) {
                            try {
                                SendbirdUIKit.uikitConfigRepo.requestConfigurationsBlocking(sendbirdChat, appInfo.getUiKitConfigInfo());
                            } catch (Exception e) {
                                Logger.w(e);
                            }
                        }
                    }
                }

                return new Pair<>(user, error);
            }

            @Override
            public void onResultForUiThread(@Nullable Pair<User, SendbirdException> data, @Nullable SendbirdException e) {
                final User user = data != null ? data.first : null;
                final SendbirdException error = data != null ? data.second : e;
                Logger.d("++ user=%s, error=%s", user, error);
                if (handler != null) {
                    handler.onConnected(user, error);
                }
            }
        });
    }

    @WorkerThread
    @VisibleForTesting
    static void fetchTemplatesBlocking(@NonNull SendbirdChatContract sendbirdChat) {
        final AppInfo appInfo = sendbirdChat.getAppInfo();
        if (appInfo != null) {
            ExecutorService executor = Executors.newFixedThreadPool(3); // 3 threads for 3 requests
            List<Callable<Object>> tasks = new ArrayList<>();

            final NotificationInfo notificationInfo = appInfo.getNotificationInfo();
            if (notificationInfo != null && notificationInfo.isEnabled()) {
                // Even if the request fails, it should not affect the result of the connection request.
                tasks.add(Executors.callable(() -> {
                    try {
                        // if the cache exists or no need to update, blocking is released right away
                        final String latestToken = notificationInfo.getTemplateListToken();
                        NotificationChannelManager.requestTemplateListBlocking(latestToken);
                    } catch (Exception ignore) {
                    }
                }));
                tasks.add(Executors.callable(() -> {
                    try {
                        // if the cache exists or no need to update, blocking is released right away
                        final long settingsUpdatedAt = notificationInfo.getSettingsUpdatedAt();
                        NotificationChannelManager.requestNotificationChannelSettingBlocking(settingsUpdatedAt);
                    } catch (Exception ignore) {
                    }
                }));
            }

            final MessageTemplateInfo messageTemplateInfo = appInfo.getMessageTemplateInfo();
            if (messageTemplateInfo != null && messageTemplateInfo.getToken() != null) { // `token == null` means there are no templates in server.
                tasks.add(Executors.callable(() -> {
                    try {
                        final String latestToken = messageTemplateInfo.getToken();
                        MessageTemplateManager.syncMessageTemplateListBlocking(latestToken);
                    } catch (Exception ignore) {
                    }
                }));
            }

            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException ignore) {
            } finally {
                executor.shutdown();
            }
        }
    }

    @NonNull
    private static Pair<User, SendbirdException> connectBlocking(@NonNull SendbirdChatContract sendbirdChat) throws InterruptedException {
        final AtomicReference<User> result = new AtomicReference<>();
        final AtomicReference<SendbirdException> error = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final UserInfo userInfo = adapter.getUserInfo();
        final String userId = userInfo.getUserId();
        final String accessToken = adapter.getAccessToken();
        final String apiHost = UIKitPrefs.getString(StringSet.KEY_CUSTOM_API_HOST, null);
        final String wsHost = UIKitPrefs.getString(StringSet.KEY_CUSTOM_WS_HOST, null);

        sendbirdChat.connect(userId, accessToken, apiHost, wsHost, (user, e) -> {
            result.set(user);
            if (e != null) {
                error.set(e);
            }

            latch.countDown();
        });
        latch.await();
        return new Pair<>(result.get(), error.get());
    }

    @NonNull
    private static Pair<User, SendbirdException> authenticateFeedBlocking(@NonNull SendbirdChatContract sendbirdChat) throws InterruptedException {
        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<SendbirdException> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        UserInfo userInfo = adapter.getUserInfo();
        String userId = userInfo.getUserId();
        String accessToken = adapter.getAccessToken();

        final String apiHost = UIKitPrefs.getString(StringSet.KEY_CUSTOM_API_HOST, null);
        sendbirdChat.authenticate(userId, accessToken, apiHost, (user, e) -> {
            result.set(user);
            if (e != null) {
                error.set(e);
            }

            latch.countDown();
        });
        latch.await();
        return new Pair<>(result.get(), error.get());
    }

    /**
     * Disconnects from SendbirdChat. Call this when user logged out.
     *
     * @param handler Callback handler.
     */
    public static void disconnect(@Nullable DisconnectHandler handler) {
        SendbirdChat.disconnect(() -> {
            clearAll();
            if (handler != null) {
                handler.onDisconnected();
            }
        });
    }

    /**
     * Updates current <code>UserInfo</code>.
     *
     * @param params  Params for update current users.
     * @param handler Callback handler.
     */
    public static void updateUserInfo(@NonNull UserUpdateParams params, @Nullable CompletionHandler handler) {
        SendbirdChat.updateCurrentUserInfo(params, handler);
    }

    private static void updateUserInfoBlocking(@NonNull SendbirdChatContract sendbirdChatContract, @NonNull UserUpdateParams params) throws SendbirdException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<SendbirdException> error = new AtomicReference<>();
        sendbirdChatContract.updateCurrentUserInfo(params, e -> {
            if (e != null) error.set(e);
            latch.countDown();
        });
        latch.await();
        if (error.get() != null) throw error.get();
    }

    /**
     * Sets the handler that loads the list of custom user.
     *
     * @param handler The callback that will run.
     */
    public static void setCustomUserListQueryHandler(@Nullable CustomUserListQueryHandler handler) {
        SendbirdUIKit.customUserListQueryHandler = handler;
    }

    /**
     * Sets the handler so that common custom data can be set.
     *
     * @param handler The callback that will run.
     * since 1.2.2
     */
    public static void setCustomParamsHandler(@Nullable CustomParamsHandler handler) {
        SendbirdUIKit.customParamsHandler = handler;
    }

    /**
     * Sets the edge-to-edge config for customizing the status bar color.
     *
     * @param config The configuration to be applied for custom status bar color on edge-to-edge
     * since 3.23.0
     */
    public static void setEdgeToEdgeConfig(@NonNull EdgeToEdgeConfig config) {
        edgeToEdgeConfig = config;
    }


    /**
     * Sets the factory that creates fragments generated by UIKit's basic activities.
     * <p>
     * @deprecated 3.9.0
     * <p> Use {@link com.sendbird.uikit.providers.FragmentProviders} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    public static void setUIKitFragmentFactory(@NonNull UIKitFragmentFactory factory) {
        SendbirdUIKit.fragmentFactory = factory;
    }

    /**
     * Returns the factory that creates fragments generated by UIKit's basic activities.
     *
     * @return {@link UIKitFragmentFactory} that creates fragments generated by UIKit's basic activities.
     * @deprecated 3.9.0
     * <p> Use {@link com.sendbird.uikit.providers.FragmentProviders} instead.</p>
     * since 3.0.0
     */
    @Deprecated
    @NonNull
    public static UIKitFragmentFactory getFragmentFactory() {
        return fragmentFactory;
    }

    private static void updateEmojiList() {
        Logger.d(">> SendBirdUIkit::updateEmojiList()");
        SendbirdChat.getAllEmoji((emojiContainer, e) -> {
            if (e != null) {
                Logger.e(e);
            } else {
                if (emojiContainer != null) {
                    EmojiManager.upsertEmojiContainer(emojiContainer);
                }
            }
        });
    }

    /**
     * Sets whether the image file compress when trying to send image file message. Default value is <code>true</code>.
     * The target image types are 'image/jpg`, `image/jpeg`, and `image/png`, the others will be ignored.
     *
     * @param useCompression If <code>true</code> the image file will be transferred to the original image, <code>false</code> other wise.
     * since 2.0.1
     */
    public static void setUseImageCompression(boolean useCompression) {
        SendbirdUIKit.useCompression = useCompression;
    }

    /**
     * Returns the value whether the sending image file will be compressing.
     *
     * @return the value whether the sending image file will be compressing.
     * since 2.0.1
     */
    public static boolean shouldUseImageCompression() {
        return SendbirdUIKit.useCompression;
    }

    /**
     * Image compression quality value that will be used when sending image. Default value is 70.
     * It has to be bigger than 0 and cannot exceed 100.
     *
     * @param compressQuality Hint to the compressor, 0-100. 0 meaning compress for
     *                        smallest size, 100 meaning compress for max quality. Some
     *                        formats, like PNG which is lossless, will ignore the
     *                        quality setting
     * @see android.graphics.Bitmap#compress(Bitmap.CompressFormat format, int quality, OutputStream stream)
     * since 2.0.1
     */
    public static void setCompressQuality(int compressQuality) {
        SendbirdUIKit.compressQuality = compressQuality;
    }

    /**
     * Returns the value of image compression.
     *
     * @return The value of image compression.
     * since 2.0.1
     */
    public static int getCompressQuality() {
        return SendbirdUIKit.compressQuality;
    }

    /**
     * Image resizing size value that will be used when displaying image. Default resizing size values are set to 1080x1920.
     * The first value is width, the second is height.
     * When drawing a thumbnail, half the set size is used, and the minimum value is 100x100.
     *
     * @param resizingSize The value of the image to resize.
     * since 2.0.1
     */
    public static void setResizingSize(@NonNull Pair<Integer, Integer> resizingSize) {
        SendbirdUIKit.resizingSize = resizingSize;
    }

    /**
     * Returns a size value to resize.
     *
     * @return The value of the image to resize.
     * since 2.0.1
     */
    @NonNull
    public static Pair<Integer, Integer> getResizingSize() {
        return SendbirdUIKit.resizingSize;
    }

    /**
     * Returns the user mention configuration.
     *
     * @return The configuration applied for the user mention
     * since 3.0.0
     */
    @NonNull
    public static UserMentionConfig getUserMentionConfig() {
        return userMentionConfig;
    }

    /**
     * Sets the user mention configuration.
     *
     * @param config The configuration to be applied for the mention
     * @see UserMentionConfig
     * since 3.0.0
     */
    public static void setMentionConfig(@NonNull UserMentionConfig config) {
        SendbirdUIKit.userMentionConfig = config;
    }

    /**
     * Returns the voice recorder configuration.
     *
     * @return The configuration applied for the voice recorder
     * since 3.9.2
     */
    @NonNull
    public static VoiceRecorderConfig getVoiceRecorderConfig() {
        return voiceRecorderConfig;
    }

    /**
     * Sets the voice recorder configuration.
     *
     * @param config The configuration to be applied for the voice recorder
     * @see VoiceRecorderConfig
     * since 3.9.2
     */
    public static void setVoiceRecorderConfig(@NonNull VoiceRecorderConfig config) {
        SendbirdUIKit.voiceRecorderConfig = config;
    }

    /**
     * @param level set the displaying log level. {@link LogLevel}
     * since 1.0.2
     */
    public static void setLogLevel(@NonNull LogLevel level) {
        Logger.setLogLevel(level.getLevel());
    }

    /**
     * Returns the default theme mode.
     *
     * @see #setDefaultThemeMode(ThemeMode)
     */
    @NonNull
    public static ThemeMode getDefaultThemeMode() {
        return defaultThemeMode;
    }

    /**
     * Sets the default theme mode. This is the default value used for all components, but can
     * be overridden locally via the builder of a fragment.
     *
     * <p>Defaults to {@link ThemeMode#Light}.</p>
     *
     * @see #getDefaultThemeMode() ()
     */
    public static void setDefaultThemeMode(@NonNull ThemeMode themeMode) {
        defaultThemeMode = themeMode;
    }

    /**
     * Determines whether the theme mode is {@link ThemeMode#Dark}.
     *
     * @return True if the theme mode is the dark, false otherwise.
     */
    public static boolean isDarkMode() {
        return defaultThemeMode == ThemeMode.Dark;
    }

    /**
     * Returns the adapter of SendBirdUIKit.
     *
     * @see #init(SendbirdUIKitAdapter, Context)
     */
    @Nullable
    public static SendbirdUIKitAdapter getAdapter() {
        return adapter;
    }

    /**
     * Returns the custom user list query handler.
     *
     * @return The callback handler.
     */
    @Nullable
    public static CustomUserListQueryHandler getCustomUserListQueryHandler() {
        return customUserListQueryHandler;
    }

    /**
     * Returns the custom params handler.
     *
     * @return The callback handler.
     * since 1.2.2
     */
    @Nullable
    public static CustomParamsHandler getCustomParamsHandler() {
        return customParamsHandler;
    }

    /**
     * Returns the edge-to-edge config for customizing the status bar color.
     *
     * @return The configuration applied for custom status bar color on edge-to-edge
     * since 3.23.0
     */
    @NonNull
    public static EdgeToEdgeConfig getEdgeToEdgeConfig() {
        return edgeToEdgeConfig;
    }

    /**
     * Initiates a group channel with the provided bot ID.
     *
     * @param botId The bot ID that is created in dashboard.
     * @param isDistinct Determines whether to reuse an existing channel or create a new channel.
     * @param handler The callback handler that lets you know if the request was successful or not.
     * since 3.8.0
     */
    public static void startChatWithAiBot(@NonNull Context context, @NonNull String botId, boolean isDistinct, @Nullable CompletionHandler handler) {
        User currentUser = SendbirdChat.getCurrentUser();
        if (currentUser == null) {
            if (handler != null) {
                handler.onResult(new SendbirdConnectionRequiredException("Current user is null", null));
            }
            return;
        }

        GroupChannelCreateParams groupChannelCreateParams = new GroupChannelCreateParams();
        groupChannelCreateParams.setDistinct(isDistinct);
        groupChannelCreateParams.setUserIds(Arrays.asList(botId, currentUser.getUserId()));
        GroupChannel.createChannel(groupChannelCreateParams, (groupChannel, e1) -> {
            if (e1 != null) {
                if (handler != null) {
                    handler.onResult(e1);
                }
                return;
            }

            Intent intent = ChannelActivity.newIntent(context, groupChannel.getUrl());
            context.startActivity(intent);
        });
    }

    /**
     * Context switching is performed to the main thread.
     *
     * @param runnable The Runnable that will be executed.
     * since 3.6.0
     */
    public static void runOnUIThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    @TestOnly
    @Nullable
    static UIKitConfigRepository getUikitConfigRepo() {
        return uikitConfigRepo;
    }
}
