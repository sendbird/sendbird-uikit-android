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
import com.sendbird.uikit.fragments.InviteChannelFragment;
import com.sendbird.uikit.utils.ContextUtils;
import com.sendbird.uikit.utils.TextUtils;

/**
 * Activity displays a list of users and provide a current user to invite other users.
 */
public class InviteChannelActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link InviteChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return InviteChannelActivity Intent
     */
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, InviteChannelActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link InviteChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.1.2
     */
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends InviteChannelActivity> cls, @NonNull String channelUrl) {
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
            InviteChannelFragment fragment = createInviteChannelFragment(url);

            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();
            manager.beginTransaction()
                    .replace(R.id.sb_fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * It will be called when the InviteChannelActiviy is being created.
     * @return a new invite channel fragment.
     *
     * @since 1.0.4
     */
    protected InviteChannelFragment createInviteChannelFragment(@NonNull String channelUrl) {
        return new InviteChannelFragment.Builder(channelUrl)
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_invite_member))
                .setInviteButtonText(getString(R.string.sb_text_button_invite))
                .build();
    }
}
