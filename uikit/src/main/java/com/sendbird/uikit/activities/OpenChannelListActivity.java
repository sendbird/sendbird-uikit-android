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

/**
 * Activity displays settings of a channel.
 * since 3.2.0
 */
public class OpenChannelListActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return OpenChannelListActivity Intent.
     * since 3.2.0
     * @deprecated 3.22.2
     * <p> Use {@link OpenChannelListActivity#newIntent(Context)} instead.</p>
     */
    @Deprecated
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, OpenChannelListActivity.class);
    }

    /**
     * Create an intent for a {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @param themeResId the resource identifier for custom theme.
     * @return OpenChannelListActivity Intent.
     * since 3.5.6
     * @deprecated 3.22.2
     * <p> Use {@link OpenChannelListActivity#newIntent(Context, int)} instead.</p>
     */
    @Deprecated
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl, @StyleRes int themeResId) {
        return newIntentFromCustomActivity(context, OpenChannelListActivity.class, themeResId);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param cls        The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 3.2.0
     * @deprecated 3.22.2
     * <p> Use {@link OpenChannelListActivity#newIntentFromCustomActivity(Context, Class)} instead.</p>
     */
    @Deprecated
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends OpenChannelListActivity> cls, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, cls, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param cls        The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @param themeResId the resource identifier for custom theme.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 3.5.6
     * @deprecated 3.22.2
     * <p> Use {@link OpenChannelListActivity#newIntentFromCustomActivity(Context, Class, int)} instead.</p>
     */
    @Deprecated
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends OpenChannelListActivity> cls, @NonNull String channelUrl, @StyleRes int themeResId) {
        return newIntentFromCustomActivity(context, cls, themeResId);
    }

    /**
     * Create an intent for a {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @return OpenChannelListActivity Intent.
     * since 3.22.2
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return newIntentFromCustomActivity(context, OpenChannelListActivity.class);
    }

    /**
     * Create an intent for a {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param themeResId the resource identifier for custom theme.
     * @return OpenChannelListActivity Intent.
     * since 3.22.2
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @StyleRes int themeResId) {
        return newIntentFromCustomActivity(context, OpenChannelListActivity.class, themeResId);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param cls        The activity class that is to be used for the intent.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 3.22.2
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends OpenChannelListActivity> cls) {
        return newIntentFromCustomActivity(context, cls, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link OpenChannelListActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param cls        The activity class that is to be used for the intent.
     * @param themeResId the resource identifier for custom theme.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 3.22.2
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends OpenChannelListActivity> cls,  @StyleRes int themeResId) {
        Intent intent = new Intent(context, cls);
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
    }

    /**
     * It will be called when the {@link OpenChannelListActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.sendbird.uikit.fragments.OpenChannelListFragment}
     * since 3.2.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Bundle args = getIntent() != null && getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newOpenChannelListFragment(args);
    }
}
