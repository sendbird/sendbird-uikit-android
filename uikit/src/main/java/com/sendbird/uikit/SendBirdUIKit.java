package com.sendbird.uikit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.AppInfo;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.handlers.InitResultHandler;
import com.sendbird.uikit.adapter.SendBirdUIKitAdapter;
import com.sendbird.uikit.consts.ReplyType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.interfaces.CustomParamsHandler;
import com.sendbird.uikit.interfaces.CustomUserListQueryHandler;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.model.EmojiManager;
import com.sendbird.uikit.tasks.JobResultTask;
import com.sendbird.uikit.tasks.TaskQueue;
import com.sendbird.uikit.utils.FileUtils;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit.utils.UIKitPrefs;

import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sendbird UIKit Main Class.
 */
public class SendBirdUIKit {
    private static volatile SendBirdUIKitAdapter adapter;

    /**
     * UIKit log level. It depends on android Log level.
     */
    public enum LogLevel {
        ALL(Log.VERBOSE), INFO(Log.INFO), WARN(Log.WARN), ERROR(Log.ERROR);

        int level;
        LogLevel(int level) {
            this.level = level;
        }
        int getLevel() {
            return level;
        }
    }

    /**
     * UIKit theme mode.
     */
    public enum ThemeMode {
        Light(R.style.SendBird, R.color.primary_300, R.color.secondary_300, R.color.onlight_03),
        Dark(R.style.SendBird_Dark, R.color.primary_200, R.color.secondary_200, R.color.ondark_03);

        @StyleRes int resId;
        @ColorRes int primaryTintColorResId;
        @ColorRes int secondaryTintColorResId;
        @ColorRes int monoTintColorResId;

        ThemeMode(@StyleRes int resId, @ColorRes int primaryTintColorResId, @ColorRes int secondaryTintColorResId, @ColorRes int monoTintColorResId) {
            this.resId = resId;
            this.primaryTintColorResId = primaryTintColorResId;
            this.secondaryTintColorResId = secondaryTintColorResId;
            this.monoTintColorResId = monoTintColorResId;
        }

        @StyleRes
        public int getResId() {
            return resId;
        }

        @ColorRes
        public int getPrimaryTintResId() {
            return primaryTintColorResId;
        }

        @ColorRes
        public int getSecondaryTintResId() {
            return secondaryTintColorResId;
        }

        @ColorRes
        public int getMonoTintResId() {
            return monoTintColorResId;
        }

        public ColorStateList getPrimaryTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, primaryTintColorResId);
        }

        public ColorStateList getSecondaryTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, secondaryTintColorResId);
        }

        public ColorStateList getMonoTintColorStateList(@NonNull Context context) {
            return AppCompatResources.getColorStateList(context, monoTintColorResId);
        }
    }

    private static final int DEFAULT_RESIZING_WIDTH_SIZE = 1080;
    private static final int DEFAULT_RESIZING_HEIGHT_SIZE = 1920;

    private static volatile ThemeMode defaultThemeMode = ThemeMode.Light;
    private static volatile boolean useDefaultUserProfile = false;
    private static volatile boolean useCompression = false;
    private static CustomUserListQueryHandler customUserListQueryHandler;
    private static CustomParamsHandler customParamsHandler;
    private static int compressQuality = 100;
    private static Pair<Integer, Integer> resizingSize;
    private static ReplyType replyType = ReplyType.QUOTE_REPLY;

    static void clearAll() {
        SendBirdUIKit.customUserListQueryHandler = null;
        defaultThemeMode = ThemeMode.Light;
        UIKitPrefs.clearAll();
    }

    /**
     * Initializes Sendbird with given app ID.
     *
     * @param adapter The {@link SendBirdUIKitAdapter} providing an app ID, a information of the user.
     * @param context <code>Context</code> of <code>Application</code>.
     */
    public synchronized static void init(@NonNull SendBirdUIKitAdapter adapter, @NonNull Context context) {
        init(adapter, context, false);
    }

    /**
     * Initializes Sendbird from foreground with given app ID.
     *
     * @param adapter The {@link SendBirdUIKitAdapter} providing an app ID, a information of the user.
     * @param context <code>Context</code> of <code>Application</code>.
     * @since 2.1.8
     */
    public synchronized static void initFromForeground(@NonNull SendBirdUIKitAdapter adapter, @NonNull Context context) {
        init(adapter, context, true);
    }

    private synchronized static void init(@NonNull SendBirdUIKitAdapter adapter, @NonNull Context context, boolean isForeground) {
        SendBirdUIKit.adapter = adapter;
        SendBirdUIKit.setResizingSize(new Pair<>(DEFAULT_RESIZING_WIDTH_SIZE, DEFAULT_RESIZING_HEIGHT_SIZE));

        final InitResultHandler handler = adapter.getInitResultHandler();
        final InitResultHandler initResultHandler = new InitResultHandler() {
            @Override
            public void onMigrationStarted() {
                Logger.d(">> onMigrationStarted()");
                if (handler != null) {
                    handler.onMigrationStarted();
                }
            }

            @Override
            public void onInitFailed(SendBirdException e) {
                Logger.d(">> onInitFailed() e=%s", e);
                Logger.e(e);
                if (handler != null) {
                    handler.onInitFailed(e);
                }
            }

            @Override
            public void onInitSucceed() {
                Logger.d(">> onInitSucceed()");
                FileUtils.removeDeletableDir(context.getApplicationContext());
                UIKitPrefs.init(context.getApplicationContext());
                EmojiManager.getInstance().init();

                try {
                    SendBird.addExtension(StringSet.sb_uikit, BuildConfig.VERSION_NAME);
                } catch (Throwable ignored) {}

                if (handler != null) {
                    handler.onInitSucceed();
                }
            }
        };

        if (isForeground) {
            SendBird.initFromForeground(adapter.getAppId(), context, true, initResultHandler);
        } else {
            SendBird.init(adapter.getAppId(), context, true, initResultHandler);
        }
    }

    /**
     *
     * @param level set the displaying log level. {@link LogLevel}
     * @since 1.0.2
     */
    public static void setLogLevel(@NonNull LogLevel level) {
        Logger.setLogLevel(level.getLevel());
    }

    /**
     * Returns the default theme mode.
     *
     * @see #setDefaultThemeMode(ThemeMode)
     */
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
    public static void setDefaultThemeMode(ThemeMode themeMode) {
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
     * @see #init(SendBirdUIKitAdapter, Context)
     */
    public static SendBirdUIKitAdapter getAdapter() {
        return adapter;
    }

    public static CustomUserListQueryHandler getCustomUserListQueryHandler() {
        return customUserListQueryHandler;
    }

    /**
     * Returns the custom params handler.
     *
     * @return The callback handler.
     * @since 1.2.2
     */
    public static CustomParamsHandler getCustomParamsHandler() {
        return customParamsHandler;
    }

    /**
     * Returns set value whether the user profile uses.
     *
     * @return the value whether the user profile uses.
     * @since 1.2.2
     */
    public static boolean shouldUseDefaultUserProfile() {
        return useDefaultUserProfile;
    }

    /**
     * Sets whether the user profile uses.
     *
     * @param useDefaultUserProfile If <code>true</code> the default user profile included the UIKit will be shown, <code>false</code> other wise.
     * @since 1.2.2
     */
    public static void setUseDefaultUserProfile(boolean useDefaultUserProfile) {
        SendBirdUIKit.useDefaultUserProfile = useDefaultUserProfile;
    }

    /**
     * Connects to Sendbird with given <code>UserInfo</code>.
     *
     * @param handler Callback handler.
     */
    public static void connect(SendBird.ConnectHandler handler) {
        TaskQueue.addTask(new JobResultTask<User>() {
            @Override
            public User call() throws Exception {
                User user = connect();
                if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
                    UserInfo userInfo = adapter.getUserInfo();
                    String userId = userInfo.getUserId();
                    String nickname = TextUtils.isEmpty(userInfo.getNickname()) ? user.getNickname() : userInfo.getNickname();
                    if (TextUtils.isEmpty(nickname)) nickname = userId;
                    String profileUrl = TextUtils.isEmpty(userInfo.getProfileUrl()) ? user.getProfileUrl() : userInfo.getProfileUrl();
                    if (!nickname.equals(user.getNickname()) || (!TextUtils.isEmpty(profileUrl) && !profileUrl.equals(user.getProfileUrl()))) {
                        updateUserInfoBlocking(nickname, profileUrl);
                    }

                    Logger.dev("++ user nickname = %s, profileUrl = %s", user.getNickname(), user.getProfileUrl());

                    AppInfo appInfo = SendBird.getAppInfo();
                    if (appInfo != null &&
                            appInfo.useReaction() &&
                            appInfo.needUpdateEmoji(EmojiManager.getInstance().getEmojiHash())) {
                        updateEmojiList();
                    }
                }

                return user;
            }

            @Override
            public void onResultForUiThread(User user, SendBirdException e) {
                if (handler != null) {
                    handler.onConnected(SendBird.getCurrentUser(), e);
                }
            }
        });
    }

    private static User connect() throws InterruptedException, SendBirdException {
        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<SendBirdException> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        UserInfo userInfo = adapter.getUserInfo();
        String userId = userInfo.getUserId();
        String accessToken = adapter.getAccessToken();

        SendBird.connect(userId, accessToken, (user, e) -> {
            result.set(user);
            if (e != null) {
                error.set(e);
                latch.countDown();
                return;
            }

            latch.countDown();
        });
        latch.await();

        if (error.get() != null) throw error.get();
        return result.get();
    }

    /**
     * Disconnects from SendBird. Call this when user logged out.
     *
     * @param handler Callback handler.
     */
    public static void disconnect(SendBird.DisconnectHandler handler) {
        SendBird.disconnect(() -> {
            clearAll();
            if (handler != null) {
                handler.onDisconnected();
            }
        });
    }

    /**
     * Updates current <code>UserInfo</code>.
     *
     * @param nickname   Nickname to be used.
     * @param profileUrl Image URL to be used.
     * @param handler    Callback handler.
     */
    public static void updateUserInfo(String nickname, String profileUrl, SendBird.UserInfoUpdateHandler handler) {
        SendBird.updateCurrentUserInfo(nickname, profileUrl, handler);
    }

    private static void updateUserInfoBlocking(String nickname, String profileUrl) throws SendBirdException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<SendBirdException> error = new AtomicReference<>();
        SendBird.updateCurrentUserInfo(nickname, profileUrl, e -> {
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
    public static void setCustomUserListQueryHandler(CustomUserListQueryHandler handler) {
        SendBirdUIKit.customUserListQueryHandler = handler;
    }

    /**
     * Sets the handler so that common custom data can be set.
     *
     * @param handler The callback that will run.
     * @since 1.2.2
     */
    public static void setCustomParamsHandler(CustomParamsHandler handler) {
        SendBirdUIKit.customParamsHandler = handler;
    }

    private static void updateEmojiList() {
        Logger.d(">> SendBirdUIkit::updateEmojiList()");
        SendBird.getAllEmoji((emojiContainer, e) -> {
            if (e != null) {
                Logger.e(e);
            } else {
                EmojiManager.getInstance().upsertEmojiContainer(emojiContainer);
            }
        });
    }

    /**
     * Sets whether the image file compress when trying to send image file message. Default value is <code>false</code>.
     * The target image types are 'image/jpg`, `image/jpeg`, and `image/png`, the others will be ignored.
     *
     * @param useCompression If <code>true</code> the image file will be transferred to the original image, <code>false</code> other wise.
     * @since 2.0.1
     */
    public static void setUseImageCompression(boolean useCompression) {
        SendBirdUIKit.useCompression = useCompression;
    }

    /**
     * Returns the value whether the sending image file will be compressing.
     *
     * @return the value whether the sending image file will be compressing.
     * @since 2.0.1
     */
    public static boolean shouldUseImageCompression() {
        return SendBirdUIKit.useCompression;
    }

    /**
     * Image compression quality value that will be used when sending image. Default value is 100.
     * It has to be bigger than 0 and cannot exceed 100.
     *
     * @param compressQuality Hint to the compressor, 0-100. 0 meaning compress for
     *                        small size, 100 meaning compress for max quality. Some
     *                        formats, like PNG which is lossless, will ignore the
     *                        quality setting
     * @since 2.0.1
     * @see android.graphics.Bitmap#compress( Bitmap.CompressFormat format, int quality, OutputStream stream)
     */
    public static void setCompressQuality(int compressQuality) {
        SendBirdUIKit.compressQuality = compressQuality;
    }

    /**
     * Returns the value of image compression.
     *
     * @return The value of image compression.
     * @since 2.0.1
     */
    public static int getCompressQuality() {
        return SendBirdUIKit.compressQuality;
    }

    /**
     * Image resizing size value that will be used when displaying image. Default resizing size values are set to 1080x1920.
     * The first value is width, the second is height.
     * When drawing a thumbnail, half the set size is used, and the minimum value is 100x100.
     *
     * @param resizingSize The value of the image to resize.
     * @since 2.0.1
     */
    public static void setResizingSize(@NonNull Pair<Integer, Integer> resizingSize) {
        SendBirdUIKit.resizingSize = resizingSize;
    }

    /**
     * Returns a size value to resize.
     *
     * @return The value of the image to resize.
     * @since 2.0.1
     */
    public static Pair<Integer, Integer> getResizingSize() {
        return SendBirdUIKit.resizingSize;
    }

    /**
     * Sets <code>ReplyType</code>, which is how replies are displayed in the message list.
     *
     * @since 2.2.0
     */
    public static void setReplyType(@NonNull ReplyType replyType) {
        SendBirdUIKit.replyType = replyType;
    }

    /**
     * Returns <code>ReplyType</code>, which is how replies are displayed in the message list.
     *
     * @return The value of <code>ReplyType</code>.
     * @since 2.2.0
     */
    @NonNull
    public static ReplyType getReplyType() {
        return SendBirdUIKit.replyType;
    }
}
