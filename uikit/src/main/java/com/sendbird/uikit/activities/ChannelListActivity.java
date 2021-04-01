package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.ChannelListFragment;
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
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, ChannelListActivity.class);
    }

    /**
     * Create an intent for a {@link ChannelListActivity}.
     * This intent will redirect to {@link ChannelActivity} if channel url is valid.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return ChannelListActivity Intent
     */
    public static Intent newRedirectToChannelIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, ChannelListActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link ChannelListActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.1.2
     */
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends ChannelListActivity> cls, @NonNull String channelUrl) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendBirdUIKit.isDarkMode() ? R.style.SendBird_Dark : R.style.SendBird);
        setContentView(R.layout.sb_activity);

        ChannelListFragment fragment = createChannelListFragment();

        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(R.id.sb_fragment_container, fragment)
                .commit();

        redirectChannelIfNeeded(getIntent());
    }

    /**
     * It will be called when the ChannelListActiviy is being created.
     * @return a new channel list fragment
     *
     * @since 1.0.4
     */
    protected ChannelListFragment createChannelListFragment() {
        return new ChannelListFragment.Builder()
                .setUseHeader(true)
                .build();
    }

    /**
     * It will be called when it needs to redirect {@link ChannelActivity} from the push notification
     * @return ChannelActivity {@link Intent}
     *
     * @since 1.0.4
     */
    protected Intent createRedirectChannelActivityIntent(@NonNull String channelUrl) {
        return ChannelActivity.newIntent(this, channelUrl);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        redirectChannelIfNeeded(intent);
    }

    private void redirectChannelIfNeeded(Intent intent) {
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
