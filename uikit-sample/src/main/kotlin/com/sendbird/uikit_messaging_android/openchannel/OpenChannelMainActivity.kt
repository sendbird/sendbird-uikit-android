package com.sendbird.uikit_messaging_android.openchannel

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.params.OpenChannelListQueryParams
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.fragments.OpenChannelListFragment
import com.sendbird.uikit_messaging_android.R
import com.sendbird.uikit_messaging_android.SettingsFragment
import com.sendbird.uikit_messaging_android.consts.StringSet
import com.sendbird.uikit_messaging_android.databinding.ActivityOpenChannelMainBinding
import com.sendbird.uikit_messaging_android.openchannel.community.CommunityListFragment
import com.sendbird.uikit_messaging_android.openchannel.livestream.LiveStreamListAdapter
import com.sendbird.uikit_messaging_android.openchannel.livestream.LiveStreamListFragment
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
import com.sendbird.uikit_messaging_android.widgets.CustomTabView

/**
 * Displays an open channel list screen.
 */
class OpenChannelMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpenChannelMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeResId = SendbirdUIKit.getDefaultThemeMode().resId
        setTheme(themeResId)
        binding = ActivityOpenChannelMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        initPage()
    }

    private fun initPage() {
        val isDark = PreferenceUtils.isUsingDarkTheme
        binding.background.setBackgroundResource(if (isDark) R.color.background_600 else R.color.background_50)
        binding.tbMain.setBackgroundResource(if (isDark) R.color.background_500 else R.color.background_50)
        binding.tbMain.setTitleTextColor(
            ResourcesCompat.getColor(
                resources,
                if (isDark) R.color.ondark_01 else R.color.onlight_01,
                null
            )
        )
        binding.tvDescription.setTextColor(
            ResourcesCompat.getColor(
                resources,
                if (isDark) R.color.ondark_02 else R.color.onlight_02,
                null
            )
        )
        setSupportActionBar(binding.tbMain)
        binding.vpMain.adapter = MainAdapter(this)
        val isDarkMode = PreferenceUtils.isUsingDarkTheme
        val backgroundRedId = if (isDarkMode) R.color.background_600 else R.color.background_50
        binding.tlMain.setBackgroundResource(backgroundRedId)
        val liveStreamTab = CustomTabView(this)
        liveStreamTab.setBadgeVisibility(View.GONE)
        liveStreamTab.setTitle(getString(R.string.text_live_streams))
        liveStreamTab.setIcon(R.drawable.icon_streaming)
        val communityTab = CustomTabView(this)
        communityTab.setBadgeVisibility(View.GONE)
        communityTab.setTitle(getString(R.string.text_community))
        communityTab.setIcon(R.drawable.icon_channels)
        val settingsTab = CustomTabView(this)
        settingsTab.setBadgeVisibility(View.GONE)
        settingsTab.setTitle(getString(R.string.text_tab_settings))
        settingsTab.setIcon(R.drawable.icon_settings_filled)
        TabLayoutMediator(binding.tlMain, binding.vpMain) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.customView = liveStreamTab
                1 -> tab.customView = communityTab
                2 -> tab.customView = settingsTab
                else -> {}
            }
        }.attach()
        binding.tvDescription.visibility = View.VISIBLE
        binding.tvDescription.setText(R.string.text_live_streaming_description)
        setActionBarTitle(getString(R.string.text_live_streams))
        binding.vpMain.offscreenPageLimit = 3
        binding.vpMain.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            /**
             * This method will be invoked when a new page becomes selected. Animation is not
             * necessarily complete.
             *
             * @param position Position index of the new selected page.
             */
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.tvDescription.visibility = View.VISIBLE
                        binding.tvDescription.setText(R.string.text_live_streaming_description)
                        setActionBarTitle(getString(R.string.text_live_streams))
                    }

                    1 -> {
                        binding.tvDescription.visibility = View.GONE
                        setActionBarTitle(getString(R.string.text_community))
                    }

                    else -> {
                        binding.tvDescription.visibility = View.GONE
                        setActionBarTitle(getString(R.string.text_tab_settings))
                    }
                }
            }
        })
    }

    private fun setActionBarTitle(title: String) {
        val actionBar = supportActionBar ?: return
        actionBar.title = title
    }

    /**
     * @param fragmentActivity if the ViewPager2 lives directly in a [FragmentActivity] subclass.
     * @see FragmentStateAdapter
     */
    private class MainAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val params = OpenChannelListQueryParams()
                    params.customTypeFilter = StringSet.SB_LIVE_TYPE
                    OpenChannelListFragment.Builder()
                        .setCustomFragment(LiveStreamListFragment())
                        .setOpenChannelListAdapter(
                            LiveStreamListAdapter()
                        )
                        .setUseHeader(false)
                        .setUseRefreshLayout(false)
                        .setCustomQueryParams(params)
                        .build()
                }

                1 -> {
                    val params = OpenChannelListQueryParams()
                    params.customTypeFilter = StringSet.SB_COMMUNITY_TYPE
                    OpenChannelListFragment.Builder()
                        .setCustomFragment(CommunityListFragment())
                        .setUseHeader(false)
                        .setCustomQueryParams(params)
                        .build()
                }

                else -> {
                    val fragment = SettingsFragment()
                    val bundle = Bundle()
                    bundle.putBoolean(StringSet.SETTINGS_USE_HEADER, false)
                    bundle.putBoolean(StringSet.SETTINGS_USE_DO_NOT_DISTURB, false)
                    fragment.arguments = bundle
                    fragment
                }
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        override fun getItemCount(): Int {
            return PAGE_SIZE
        }

        companion object {
            private const val PAGE_SIZE = 3
        }
    }
}
