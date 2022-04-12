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
import com.sendbird.uikit.model.HighlightMessageInfo;

/**
 * Activity displays a list of messages from a channel.
 */
public class ChannelActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link ChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return ChannelActivity Intent
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, ChannelActivity.class, channelUrl);
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link ChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * @since 1.1.2
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends ChannelActivity> cls, @NonNull String channelUrl) {
        return new IntentBuilder(context, cls, channelUrl).build();
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
     * It will be called when the {@link ChannelActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.sendbird.uikit.fragments.ChannelFragment}
     * @since 3.0.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(StringSet.KEY_FROM_SEARCH_RESULT)) {
                // If the request comes from Search page, the channel settings page should hide on the header view.
                // this is a internal behavior.
                boolean useRightButton = !intent.getBooleanExtra(StringSet.KEY_FROM_SEARCH_RESULT, true);
                intent.putExtra(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useRightButton);
            }
        }
        final Bundle args = intent != null && intent.getExtras() != null ? intent.getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newChannelFragment(args.getString(StringSet.KEY_CHANNEL_URL, ""), args);
    }

    /**
     * This builder makes {@link Intent} for ChannelActivity.
     *
     * @since 2.1.0
     */
    public static class IntentBuilder {
        @NonNull
        private final Context context;
        @NonNull
        private final String channelUrl;
        private long startingPoint = Long.MAX_VALUE;
        @Nullable
        private HighlightMessageInfo highlightMessageInfo;
        @Nullable
        private Class<? extends ChannelActivity> customClass = ChannelActivity.class;

        /**
         * Create an intent for a {@link ChannelActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param channelUrl The url of the channel will be implemented.
         * @since 2.1.0
         */
        public IntentBuilder(@NonNull Context context, @NonNull String channelUrl) {
            this.context = context;
            this.channelUrl = channelUrl;
        }

        /**
         * Create an intent for a {@link ChannelActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param customClass The activity class that is to be used for the intent.
         * @param channelUrl The url of the channel will be implemented.
         * @since 2.1.0
         */
        public IntentBuilder(@NonNull Context context, @NonNull Class<? extends ChannelActivity> customClass, @NonNull String channelUrl) {
            this.context = context;
            this.channelUrl = channelUrl;
            this.customClass = customClass;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startingPoint A timestamp to load initially.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        @NonNull
        public IntentBuilder setStartingPoint(long startingPoint) {
            this.startingPoint = startingPoint;
            return this;
        }

        /**
         * Sets the information of the message to highlight.
         *
         * @param highlightMessageInfo An information of the message to highlight.
         * @return This Builder object to allow for chaining of calls to set methods.
         * @since 2.1.0
         */
        @NonNull
        public IntentBuilder setHighlightMessageInfo(@NonNull HighlightMessageInfo highlightMessageInfo) {
            this.highlightMessageInfo = highlightMessageInfo;
            return this;
        }

        /**
         * Creates an {@link Intent} with the arguments supplied to this builder.
         *
         * @return The ChannelActivity {@link Intent} applied to the {@link Bundle}.
         * @since 2.1.0
         */
        @NonNull
        public Intent build() {
            Intent intent = new Intent(context, customClass);
            intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
            intent.putExtra(StringSet.KEY_STARTING_POINT, startingPoint);
            if (highlightMessageInfo != null) {
                intent.putExtra(StringSet.KEY_HIGHLIGHT_MESSAGE_INFO, highlightMessageInfo);
            }
            return intent;
        }
    }
}
