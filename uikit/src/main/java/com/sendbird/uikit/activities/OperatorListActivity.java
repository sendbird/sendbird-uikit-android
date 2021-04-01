package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.OperatorListFragment;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.TextUtils;

/**
 * Activity displays a list of operator members from a channel.
 *
 * @since 1.2.0
 */
public class OperatorListActivity extends AppCompatActivity {
    /**
     * Create an intent for a {@link OperatorListActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.2.0
     */
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, OperatorListActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity.
     * The custom activity must inherit {@link OperatorListActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl The url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.2.0
     */
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends OperatorListActivity> cls, @NonNull String channelUrl) {
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
            Fragment fragment = createOperatorListFragment(url);

            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();
            manager.beginTransaction()
                    .replace(R.id.sb_fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * It will be called when the OperatorListActivity is being created.
     *
     * @return a new fragment to display operators in the channel.
     * @since 1.2.0
     */
    protected Fragment createOperatorListFragment(@NonNull String channelUrl) {
        return new OperatorListFragment.Builder(channelUrl)
                .setUseHeader(true)
                .setUseHeaderRightButton(true)
                .setHeaderTitle(getString(R.string.sb_text_menu_operators))
                .setEmptyIcon(R.drawable.icon_chat, SendBirdUIKit.getDefaultThemeMode().getMonoTintColorStateList(this))
                .setEmptyText(R.string.sb_text_empty_no_operator_member)
                .build();
    }
}
