package com.sendbird.uikit.activities;

import static com.sendbird.uikit.internal.extensions.ViewExtensionsKt.setInsetMarginAndStatusBarColor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.utils.TextUtils;

/**
 * Activity displays a list of channels from a current user.
 */
public class ChannelListActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link ChannelListActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @return ChannelListActivity Intent.
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return newIntent(context, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a {@link ChannelListActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param themeResId the resource identifier for custom theme.
     * @return ChannelListActivity Intent.
     * since 3.5.6
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @StyleRes int themeResId) {
        Intent intent = new Intent(context, ChannelListActivity.class);
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
    }

    /**
     * Create an intent for a {@link ChannelListActivity}.
     * This intent will redirect to {@link ChannelActivity} if channel url is valid.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return ChannelListActivity Intent
     */
    @NonNull
    public static Intent newRedirectToChannelIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, ChannelListActivity.class, channelUrl);
    }

    /**
     * Create an intent for a {@link ChannelListActivity}.
     * This intent will redirect to {@link ChannelActivity} if channel url is valid.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @param themeResId the resource identifier for custom theme.
     * @return ChannelListActivity Intent
     * since 3.5.6
     */
    @NonNull
    public static Intent newRedirectToChannelIntent(@NonNull Context context, @NonNull String channelUrl, @StyleRes int themeResId) {
        return newIntentFromCustomActivity(context, ChannelListActivity.class, channelUrl, themeResId);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link ChannelListActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 1.1.2
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends ChannelListActivity> cls, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, cls, channelUrl, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link ChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param cls        The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @param themeResId the resource identifier for custom theme.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 3.5.6
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends ChannelListActivity> cls, @NonNull String channelUrl, @StyleRes int themeResId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = getIntent().getIntExtra(StringSet.KEY_THEME_RES_ID, SendbirdUIKit.getDefaultThemeMode().getResId());
        setTheme(themeResId);
        setContentView(R.layout.sb_activity);

        View activityContainer = findViewById(R.id.sb_activity_container);
        View fragmentContainer = findViewById(R.id.sb_fragment_container);
        setInsetMarginAndStatusBarColor(activityContainer, fragmentContainer, getWindow());

        Fragment fragment = createFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
            .replace(R.id.sb_fragment_container, fragment)
            .commit();

        redirectChannelIfNeeded(getIntent());
    }

    /**
     * It will be called when the {@link ChannelListActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.sendbird.uikit.fragments.ChannelListFragment}
     * since 3.0.0
     */
    @NonNull
    protected Fragment createFragment() {
        return SendbirdUIKit.getFragmentFactory().newChannelListFragment(new Bundle());
    }

    /**
     * It will be called when it needs to redirect {@link ChannelActivity} from the push notification
     *
     * @return ChannelActivity {@link Intent}
     * since 1.0.4
     */
    @NonNull
    protected Intent createRedirectChannelActivityIntent(@NonNull String channelUrl) {
        return ChannelActivity.newIntent(this, channelUrl);
    }

    @Override
    protected void onNewIntent(@Nullable Intent intent) {
        super.onNewIntent(intent);
        redirectChannelIfNeeded(intent);
    }

    private void redirectChannelIfNeeded(@Nullable Intent intent) {
        if (intent == null) return;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra(StringSet.KEY_CHANNEL_URL);
        }
        if (intent.hasExtra(StringSet.KEY_CHANNEL_URL)) {
            String channelUrl = intent.getStringExtra(StringSet.KEY_CHANNEL_URL);
            if (!TextUtils.isEmpty(channelUrl)) startActivity(createRedirectChannelActivityIntent(channelUrl));
            intent.removeExtra(StringSet.KEY_CHANNEL_URL);
        }
    }
}
