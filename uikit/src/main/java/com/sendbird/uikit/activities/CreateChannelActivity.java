package com.sendbird.uikit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.sendbird.uikit.R;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.consts.CreateableChannelType;
import com.sendbird.uikit.consts.StringSet;
import com.sendbird.uikit.fragments.CreateChannelFragment;

/**
 * Activity displays a list of users and provide a current user to create a channel.
 */
public class CreateChannelActivity extends AppCompatActivity {
    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @return CreateChannelActivity Intent.
     */
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, CreateChannelActivity.class);
    }

    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param type The createable channel type. see the {@link CreateableChannelType}.
     * @return CreateChannelActivity Intent.
     *
     * @since 1.2.0
     */
    public static Intent newIntent(@NonNull Context context, @NonNull CreateableChannelType type) {
        return newIntentFromCustomActivity(context, CreateChannelActivity.class, type);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param type The createable channel type. see the {@link CreateableChannelType}.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.2.0
     */
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends CreateChannelActivity> cls, @NonNull CreateableChannelType type) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_SELECTED_CHANNEL_TYPE, type);
        return intent;
    }

    private CreateableChannelType channelType = CreateableChannelType.Normal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendBirdUIKit.isDarkMode() ? R.style.SendBird_Dark : R.style.SendBird);
        setContentView(R.layout.sb_activity);

        if (getIntent().hasExtra(StringSet.KEY_SELECTED_CHANNEL_TYPE)) {
            this.channelType = (CreateableChannelType) getIntent().getSerializableExtra(StringSet.KEY_SELECTED_CHANNEL_TYPE);
        }
        CreateChannelFragment fragment = createCreateChannelFragment();

        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(R.id.sb_fragment_container, fragment)
                .commit();
    }

    /**
     * It will be called when the CreateChannelActiviy is being created.
     * @return a new create channel fragment.
     *
     * @since 1.0.4
     */
    protected CreateChannelFragment createCreateChannelFragment() {
        return createCreateChannelFragment(channelType);
    }

    /**
     * It will be called when the CreateChannelActiviy is being created.
     * @param type The createable channel type. see the {@link CreateableChannelType}.
     * @return a new create channel fragment.
     *
     * @since 1.2.0
     */
    protected CreateChannelFragment createCreateChannelFragment(CreateableChannelType type) {
        return new CreateChannelFragment.Builder(type)
                .setUseHeader(true)
                .setHeaderTitle(getString(R.string.sb_text_header_create_channel))
                .setCreateButtonText(getString(R.string.sb_text_button_create))
                .build();
    }
}
