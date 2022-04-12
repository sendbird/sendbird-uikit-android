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
import com.sendbird.uikit.fragments.PromoteOperatorFragment;

/**
 * Activity displays a list of users and provide a current user.
 *
 * @since 1.2.0
 */
public class PromoteOperatorActivity extends AppCompatActivity {
    /**
     * Create an intent for a {@link PromoteOperatorActivity}.
     * @param context A Context of the application package implementing this class.
     * @param channelUrl The url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     *
     * @since 1.2.0
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, PromoteOperatorActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity.
     * The custom activity must inherit {@link PromoteOperatorActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl The url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.2.0
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends PromoteOperatorActivity> cls, @NonNull String channelUrl) {
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
     * It will be called when the {@link PromoteOperatorActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link PromoteOperatorFragment}
     * @since 3.0.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Bundle args = getIntent() != null && getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newPromoteOperatorFragment(args.getString(StringSet.KEY_CHANNEL_URL, ""), args);
    }
}
