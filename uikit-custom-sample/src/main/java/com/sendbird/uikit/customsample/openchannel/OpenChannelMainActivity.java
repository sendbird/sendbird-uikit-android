package com.sendbird.uikit.customsample.openchannel;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.sendbird.android.params.OpenChannelListQueryParams;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.SettingsFragment;
import com.sendbird.uikit.customsample.consts.StringSet;
import com.sendbird.uikit.customsample.databinding.ActivityOpenChannelMainBinding;
import com.sendbird.uikit.customsample.openchannel.community.CommunityListFragment;
import com.sendbird.uikit.customsample.openchannel.livestream.LiveStreamListAdapter;
import com.sendbird.uikit.customsample.openchannel.livestream.LiveStreamListFragment;
import com.sendbird.uikit.customsample.widgets.CustomTabView;
import com.sendbird.uikit.fragments.OpenChannelListFragment;

/**
 * Displays an open channel list screen.
 */
public class OpenChannelMainActivity extends AppCompatActivity {
    private ActivityOpenChannelMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = SendbirdUIKit.getDefaultThemeMode().getResId();
        setTheme(themeResId);
        binding = ActivityOpenChannelMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        initPage();
    }

    private void initPage() {
        setSupportActionBar(binding.tbMain);
        binding.vpMain.setAdapter(new MainAdapter(this));

        int backgroundRedId = R.color.background_50;
        binding.tlMain.setBackgroundResource(backgroundRedId);

        CustomTabView liveStreamTab = new CustomTabView(this);
        liveStreamTab.setBadgeVisibility(View.GONE);
        liveStreamTab.setTitle(getString(R.string.text_live_streams));
        liveStreamTab.setIcon(R.drawable.icon_streaming);

        CustomTabView communityTab = new CustomTabView(this);
        communityTab.setBadgeVisibility(View.GONE);
        communityTab.setTitle(getString(R.string.text_community));
        communityTab.setIcon(R.drawable.icon_channels);

        CustomTabView settingsTab = new CustomTabView(this);
        settingsTab.setBadgeVisibility(View.GONE);
        settingsTab.setTitle(getString(R.string.text_tab_settings));
        settingsTab.setIcon(R.drawable.icon_settings_filled);

        binding.tvDescription.setVisibility(View.VISIBLE);
        binding.tvDescription.setText(R.string.text_live_streaming_description);
        setActionBarTitle(getString(R.string.text_live_streams));

        new TabLayoutMediator(binding.tlMain, binding.vpMain, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setCustomView(liveStreamTab);
                    break;
                case 1:
                    tab.setCustomView(communityTab);
                    break;
                case 2:
                    tab.setCustomView(settingsTab);
                    break;
                default:
                    break;

            }
        }).attach();

        binding.vpMain.setOffscreenPageLimit(3);
        binding.vpMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.tvDescription.setVisibility(View.VISIBLE);
                    binding.tvDescription.setText(R.string.text_live_streaming_description);
                    setActionBarTitle(getString(R.string.text_live_streams));
                    binding.tbMain.setVisibility(View.VISIBLE);
                    binding.tbBorder.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    binding.tvDescription.setVisibility(View.GONE);
                    setActionBarTitle(getString(R.string.text_community));
                    binding.tbMain.setVisibility(View.VISIBLE);
                    binding.tbBorder.setVisibility(View.VISIBLE);
                } else {
                    binding.tvDescription.setVisibility(View.GONE);
                    setActionBarTitle(getString(R.string.text_tab_settings));
                    binding.tbMain.setVisibility(View.GONE);
                    binding.tbBorder.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setActionBarTitle(String title) {
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setTitle(title);
    }

    private static class MainAdapter extends FragmentStateAdapter {
        private static final int PAGE_SIZE = 3;

        /**
         * @param fragmentActivity if the {@link ViewPager2} lives directly in a
         *                         {@link FragmentActivity} subclass.
         * @see FragmentStateAdapter#FragmentStateAdapter(Fragment)
         * @see FragmentStateAdapter#FragmentStateAdapter(FragmentManager, Lifecycle)
         */
        public MainAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            if (position == 0) {
                final OpenChannelListQueryParams params = new OpenChannelListQueryParams();
                params.setCustomTypeFilter(StringSet.SB_LIVE_TYPE);
                return new com.sendbird.uikit.fragments.OpenChannelListFragment.Builder()
                        .setCustomFragment(new LiveStreamListFragment())
                        .setOpenChannelListAdapter(new LiveStreamListAdapter())
                        .setUseHeader(false)
                        .setUseRefreshLayout(false)
                        .setCustomQueryParams(params)
                        .build();
            } else if (position == 1) {
                final OpenChannelListQueryParams params = new OpenChannelListQueryParams();
                params.setCustomTypeFilter(StringSet.SB_COMMUNITY_TYPE);
                return new OpenChannelListFragment.Builder()
                        .setCustomFragment(new CommunityListFragment())
                        .setUseHeader(false)
                        .setCustomQueryParams(params)
                        .build();
            } else {
                SettingsFragment fragment = new SettingsFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, false);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return PAGE_SIZE;
        }
    }
}
