package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.consts.StringSet;

/**
 * Activity displays a list of messages from a open channel.
 * since 3.2.0
 */
public class OpenChannelActivity extends AppCompatActivity {
    /**
     * Create an intent for a {@link OpenChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return OpenChannelActivity Intent
     * since 3.2.0
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull Class<? extends OpenChannelActivity> cls, @NonNull String channelUrl) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendbirdUIKit.isDarkMode() ? R.style.AppTheme_Dark_Sendbird : R.style.AppTheme_Sendbird);
        setContentView(R.layout.sb_activity);

        Fragment fragment = createFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(R.id.sb_fragment_container, fragment)
                .commit();
    }

    /**
     * It will be called when the {@link OpenChannelActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.sendbird.uikit.fragments.OpenChannelFragment}
     * since 3.2.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Intent intent = getIntent();
        final Bundle args = intent != null && intent.getExtras() != null ? intent.getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newOpenChannelFragment(args.getString(StringSet.KEY_CHANNEL_URL, ""), args);
    }
}
