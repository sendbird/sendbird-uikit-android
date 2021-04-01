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
import com.sendbird.uikit.fragments.OpenChannelSettingsFragment;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.TextUtils;

/**
 * Activity displays settings of an open channel.
 *
 * @since 2.0.0
 */
public class OpenChannelSettingsActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link OpenChannelSettingsActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return OpenChannelSettingsActivity Intent.
     */
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, OpenChannelSettingsActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link OpenChannelSettingsActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     */
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends OpenChannelSettingsActivity> cls, @NonNull String channelUrl) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendBirdUIKit.isDarkMode() ? R.style.SendBird_Dark : R.style.SendBird);
        setContentView(R.layout.sb_activity);

        String url = getIntent().getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (TextUtils.isEmpty(url)) {
            ContextUtils.toastError(this, R.string.sb_text_error_get_channel);
        } else {
            OpenChannelSettingsFragment fragment = createOpenChannelSettingsFragment(url);

            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();
            manager.beginTransaction()
                    .replace(R.id.sb_fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * It will be called when the OpenChannelSettingActivity is being created.
     * @return a new channel settings fragment.
     */
    protected OpenChannelSettingsFragment createOpenChannelSettingsFragment(@NonNull String channelUrl) {
        return new OpenChannelSettingsFragment.Builder(channelUrl)
                .setUseHeader(true)
                .build();
    }
}
