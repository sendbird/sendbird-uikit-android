package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.PromoteOperatorsFragment;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.TextUtils;

/**
 * Activity displays a list of users and provide a current user.
 *
 * @since 1.2.0
 */
public class PromoteOperatorsActivity extends AppCompatActivity {
    /**
     * Create an intent for a {@link PromoteOperatorsActivity}.
     * @param context A Context of the application package implementing this class.
     * @param channelUrl The url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     *
     * @since 1.2.0
     */
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, PromoteOperatorsActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity.
     * The custom activity must inherit {@link PromoteOperatorsActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl The url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.2.0
     */
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends PromoteOperatorsActivity> cls, @NonNull String channelUrl) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendBirdUIKit.isDarkMode() ? R.style.SendBird_Dark : R.style.SendBird);
        setContentView(R.layout.sb_activity);

        String channelUrl = getIntent().getStringExtra(StringSet.KEY_CHANNEL_URL);
        if (TextUtils.isEmpty(channelUrl)) {
            ContextUtils.toastError(this, R.string.sb_text_error_required_channel_url);
        } else {
            GroupChannel.getChannel(channelUrl, (channel, e) -> {
                if (e != null) {
                    ContextUtils.toastError(this, R.string.sb_text_error_get_channel);
                    Logger.e(e);
                    return;
                }
                Fragment fragment = createPromoteOperatorFragment(channel);
                FragmentManager manager = getSupportFragmentManager();
                manager.popBackStack();
                manager.beginTransaction()
                        .replace(R.id.sb_fragment_container, fragment)
                        .commit();
            });
        }
    }

    /**
     * It will be called when the PromoteOperatorsActivity is being created.
     *
     * @return a new fragment to promote operators.
     * @since 1.2.0
     */
    protected Fragment createPromoteOperatorFragment(@NonNull GroupChannel channel) {
        return new PromoteOperatorsFragment.Builder(channel.getUrl())
                .setHeaderTitle(getString(R.string.sb_text_header_select_members))
                .setRightButtonText(getString(R.string.sb_text_button_add))
                .setUseHeader(true)
                .build();
    }
}
