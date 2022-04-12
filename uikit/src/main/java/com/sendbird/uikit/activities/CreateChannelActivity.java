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
import com.sendbird.uikit.consts.CreatableChannelType;
import com.sendbird.uikit.consts.StringSet;

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
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, CreateChannelActivity.class);
    }

    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param type The creatable channel type. see the {@link CreatableChannelType}.
     * @return CreateChannelActivity Intent.
     *
     * @since 1.2.0
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull CreatableChannelType type) {
        return newIntentFromCustomActivity(context, CreateChannelActivity.class, type);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param type The creatable channel type. see the {@link CreatableChannelType}.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.2.0
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends CreateChannelActivity> cls, @NonNull CreatableChannelType type) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_SELECTED_CHANNEL_TYPE, type);
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
     * It will be called when the {@link CreateChannelActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.sendbird.uikit.fragments.CreateChannelFragment}
     * @since 3.0.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Bundle args = getIntent() != null && getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        CreatableChannelType creatableChannelType = CreatableChannelType.Normal;
        if (args.containsKey(StringSet.KEY_SELECTED_CHANNEL_TYPE)) {
            creatableChannelType = (CreatableChannelType) args.getSerializable(StringSet.KEY_SELECTED_CHANNEL_TYPE);
            if (creatableChannelType == null) creatableChannelType = CreatableChannelType.Normal;
        }
        return SendbirdUIKit.getFragmentFactory().newCreateChannelFragment(creatableChannelType, args);
    }
}
