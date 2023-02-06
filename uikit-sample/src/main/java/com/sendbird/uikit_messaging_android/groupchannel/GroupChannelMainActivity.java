package com.sendbird.uikit_messaging_android.groupchannel;

import static com.sendbird.uikit_messaging_android.consts.StringSet.PUSH_REDIRECT_CHANNEL;
import static com.sendbird.uikit_messaging_android.consts.StringSet.PUSH_REDIRECT_MESSAGE_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.handler.UserEventHandler;
import com.sendbird.android.params.GroupChannelListQueryParams;
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams;
import com.sendbird.android.user.User;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.activities.ChannelActivity;
import com.sendbird.uikit.fragments.ChannelListFragment;
import com.sendbird.uikit.fragments.NotificationChannelFragment;
import com.sendbird.uikit.log.Logger;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.SettingsFragment;
import com.sendbird.uikit_messaging_android.databinding.ActivityGroupChannelMainBinding;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;
import com.sendbird.uikit_messaging_android.widgets.CustomTabView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Displays a group channel list screen.
 */
public class GroupChannelMainActivity extends AppCompatActivity {
    private static final String USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis();
    private final String notificationChannelCustomType = "SENDBIRD_NOTIFICATION_CHANNEL_NOTIFICATION";
    private static final int TAB_CHANNEL_LIST = 0;
    private static final int TAB_CHANNEL_NOTIFICATION = 1;
    private static final int TAB_SETTINGS = 2;

    private ActivityGroupChannelMainBinding binding;
    private CustomTabView channelListTab;
    private CustomTabView notificationTab;

    @NonNull
    private static String getNotificationChannelUrl() {
        final User user = SendbirdChat.getCurrentUser();
        if (user != null) {
            return "SENDBIRD_NOTIFICATION_CHANNEL_NOTIFICATION_" + user.getUserId();
        }
        throw new RuntimeException("user must exist");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SendbirdUIKit.getDefaultThemeMode().getResId());
        binding = ActivityGroupChannelMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        initPage();
    }

    private void initPage() {
        final MainAdapter adapter = new MainAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.vpMain.setAdapter(adapter);

        boolean isDarkMode = PreferenceUtils.isUsingDarkTheme();
        int backgroundRedId = isDarkMode ? R.color.background_600 : R.color.background_50;
        binding.tlMain.setBackgroundResource(backgroundRedId);
        binding.tlMain.setupWithViewPager(binding.vpMain);

        channelListTab = new CustomTabView(this);
        channelListTab.setBadgeVisibility(View.GONE);
        channelListTab.setTitle(getString(R.string.text_tab_channels));
        channelListTab.setIcon(R.drawable.icon_chat_filled);

        notificationTab = new CustomTabView(this);
        notificationTab.setBadgeVisibility(View.GONE);
        notificationTab.setTitle(getString(R.string.text_tab_notifications));
        notificationTab.setIcon(R.drawable.icon_notifications_filled);

        CustomTabView settingsTab = new CustomTabView(this);
        settingsTab.setBadgeVisibility(View.GONE);
        settingsTab.setTitle(getString(R.string.text_tab_settings));
        settingsTab.setIcon(R.drawable.icon_settings_filled);

        Objects.requireNonNull(binding.tlMain.getTabAt(TAB_CHANNEL_LIST)).setCustomView(channelListTab);
        Objects.requireNonNull(binding.tlMain.getTabAt(TAB_CHANNEL_NOTIFICATION)).setCustomView(notificationTab);
        Objects.requireNonNull(binding.tlMain.getTabAt(TAB_SETTINGS)).setCustomView(settingsTab);

        redirectChannelIfNeeded(getIntent());

        binding.tlMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int selectedPosition = binding.tlMain.getSelectedTabPosition();
                Logger.d("++ onTabSelected selected tab position =%d", selectedPosition);
                if (selectedPosition == TAB_CHANNEL_NOTIFICATION) {
                    final Fragment fragment = adapter.getItem(TAB_CHANNEL_NOTIFICATION);
                    if (fragment instanceof NotificationChannelFragment) {
                        ((NotificationChannelFragment) fragment).updateLastReadTimeOnCurrentChannel();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // initial setup
        final GroupChannelTotalUnreadMessageCountParams params = new GroupChannelTotalUnreadMessageCountParams();
        List<String> customTypeFilter = new ArrayList<>();
        customTypeFilter.add(notificationChannelCustomType);
        params.setChannelCustomTypes(customTypeFilter);
        SendbirdChat.getTotalUnreadMessageCount(params, (notificationUnreadMessageCount, e) -> {
            if (e != null) {
                return;
            }
            Logger.i("SENDBIRD_NOTIFICATION_CHANNEL_NOTIFICATION unreadCount=%s", notificationUnreadMessageCount);
            SendbirdChat.getTotalUnreadMessageCount(new GroupChannelTotalUnreadMessageCountParams(), (totalCount, e1) -> {
                if (e1 != null) {
                    return;
                }
                Logger.i("updateChannelListTab totalCount=%s, unreadCount=%s", totalCount, notificationUnreadMessageCount);
                drawUnreadCount(channelListTab, totalCount - notificationUnreadMessageCount);
                if (!notificationTab.isSelected() || notificationUnreadMessageCount == 0) {
                    drawUnreadCount(notificationTab, notificationUnreadMessageCount);
                }
            });
        });

        // It will call whenever reconnect has completed or real-time event that contains "unread_cnt" is coming.
        // App attributes must be set
        // 1. enable_admm_unread_count_payload: true
        // 2. unread_cnt_subscription : all
        // 2.1. custom_types (Register the custom type of the notification channel you are using in your app)
        SendbirdChat.addUserEventHandler(USER_EVENT_HANDLER_KEY, new UserEventHandler() {
            @Override
            public void onFriendsDiscovered(@NonNull List<User> list) {
            }

            @Override
            public void onTotalUnreadMessageCountChanged(int totalCount, @NonNull Map<String, Integer> totalCountByCustomType) {
                Integer notificationUnreadMessageCount = totalCountByCustomType.get(notificationChannelCustomType);
                if (notificationUnreadMessageCount != null) {
                    Logger.i("updateChannelListTab totalCount=%s, unreadCount=%s", totalCount, notificationUnreadMessageCount);
                    drawUnreadCount(channelListTab, totalCount - notificationUnreadMessageCount);
                    if (!notificationTab.isSelected() || notificationUnreadMessageCount == 0) {
                        drawUnreadCount(notificationTab, notificationUnreadMessageCount);
                    }
                }
            }
        });
    }

    private synchronized void drawUnreadCount(@NonNull CustomTabView tabView, int count) {
        if (count > 0) {
            tabView.setBadgeVisibility(View.VISIBLE);
            tabView.setBadgeCount(count > 99 ? getString(R.string.text_tab_badge_max_count) : String.valueOf(count));
        } else {
            tabView.setBadgeVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendbirdChat.removeUserEventHandler(USER_EVENT_HANDLER_KEY);
    }

    @Override
    protected void onNewIntent(@Nullable Intent intent) {
        super.onNewIntent(intent);
        redirectChannelIfNeeded(intent);
    }

    @NonNull
    public static Intent newRedirectToChannelIntent(@NonNull Context context, @NonNull String channelUrl, long messageId) {
        Intent intent = new Intent(context, GroupChannelMainActivity.class);
        intent.putExtra(PUSH_REDIRECT_CHANNEL, channelUrl);
        intent.putExtra(PUSH_REDIRECT_MESSAGE_ID, messageId);
        return intent;
    }

    private void redirectChannelIfNeeded(Intent intent) {
        if (intent == null) return;

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
            intent.removeExtra(PUSH_REDIRECT_MESSAGE_ID);
        }

        if (intent.hasExtra(PUSH_REDIRECT_CHANNEL)) {
            final String channelUrl = intent.getStringExtra(PUSH_REDIRECT_CHANNEL);
            intent.removeExtra(PUSH_REDIRECT_CHANNEL);
            if (channelUrl == null) return;
            if (channelUrl.equals(getNotificationChannelUrl())) {
                binding.vpMain.setCurrentItem(TAB_CHANNEL_NOTIFICATION);
                return;
            }

            if (intent.hasExtra(PUSH_REDIRECT_MESSAGE_ID)) {
                long messageId = intent.getLongExtra(PUSH_REDIRECT_MESSAGE_ID, 0L);
                if (messageId > 0L) {
                    startActivity(ChannelActivity.newRedirectToMessageThreadIntent(this, channelUrl, messageId));
                    intent.removeExtra(PUSH_REDIRECT_MESSAGE_ID);
                }
            } else {
                startActivity(ChannelActivity.newIntent(this, channelUrl));
            }
        }
    }

    private static class MainAdapter extends FragmentPagerAdapter {
        private final List<Fragment> tabItems = new ArrayList<>();

        public MainAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
            final GroupChannelListQueryParams params = new GroupChannelListQueryParams();
            final List<String> customTypesFilter = new ArrayList<>();
            customTypesFilter.add("");
            params.setCustomTypesFilter(customTypesFilter);
            final ChannelListFragment channelListFragment = new ChannelListFragment.Builder()
                    .setGroupChannelListQuery(GroupChannel.createMyGroupChannelListQuery(params))
                    .setUseHeader(true)
                    .build();
            tabItems.add(channelListFragment);
            tabItems.add(SendbirdUIKit.getFragmentFactory().newNotificationChannelFragment(getNotificationChannelUrl(), new Bundle()));
            tabItems.add(new SettingsFragment());
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return tabItems.get(position);
        }

        @Override
        public int getCount() {
            return tabItems.size();
        }
    }
}
