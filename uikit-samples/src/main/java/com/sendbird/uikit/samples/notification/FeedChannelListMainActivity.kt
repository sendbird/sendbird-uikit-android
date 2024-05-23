package com.sendbird.uikit.samples.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sendbird.android.SendbirdChat.addUserEventHandler
import com.sendbird.android.SendbirdChat.getTotalUnreadMessageCount
import com.sendbird.android.SendbirdChat.removeUserEventHandler
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.UnreadMessageCountHandler
import com.sendbird.android.handler.UserEventHandler
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.android.user.UnreadMessageCount
import com.sendbird.android.user.User
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.FeedNotificationChannelActivity
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.BaseApplication.Companion.initStateChanges
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.SampleSettingsFragment
import com.sendbird.uikit.samples.common.consts.InitState
import com.sendbird.uikit.samples.common.consts.StringSet
import com.sendbird.uikit.samples.common.extensions.getSerializable
import com.sendbird.uikit.samples.common.extensions.isUsingDarkTheme
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.CustomTabView
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivityNotificationMainBinding

class FeedChannelListMainActivity : AppCompatActivity() {
    private val binding: ActivityNotificationMainBinding by lazy {
        ActivityNotificationMainBinding.inflate(layoutInflater)
    }
    private val feedChannelListTab: CustomTabView by lazy {
        CustomTabView(this).apply {
            setBadgeVisibility(View.GONE)
            setTitle(getString(R.string.text_tab_notifications))
            setIcon(R.drawable.icon_notifications_filled)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SendbirdUIKit.getDefaultThemeMode().resId)
        setContentView(binding.root)

        // for redirecting to channel from push notification.
        initStateChanges().observe(this) { initState: InitState ->
            Logger.i("++ init state : %s", initState)
            WaitingDialog.dismiss()
            when (initState) {
                InitState.NONE -> {}
                InitState.MIGRATING -> WaitingDialog.show(this@FeedChannelListMainActivity)
                InitState.FAILED, InitState.SUCCEED -> {
                    WaitingDialog.dismiss()
                    SendbirdUIKit.connect { _, _ -> initPage() }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeUserEventHandler(USER_EVENT_HANDLER_KEY)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        redirectChannelIfNeeded(intent)
    }

    private fun initPage() {
        val adapter = MainAdapter(this@FeedChannelListMainActivity)
        binding.viewPager.adapter = adapter

        val isDarkMode = PreferenceUtils.themeMode.isUsingDarkTheme()
        val backgroundRedId = if (isDarkMode) R.color.background_600 else R.color.background_50
        binding.tabLayout.setBackgroundResource(backgroundRedId)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.customView = when (position) {
                TAB_FEED_CHANNEL_LIST -> feedChannelListTab
                TAB_SETTINGS -> CustomTabView(this).apply {
                    setBadgeVisibility(View.GONE)
                    setTitle(getString(R.string.text_tab_settings))
                    setIcon(R.drawable.icon_settings_filled)
                }
                else -> null
            }
        }.attach()
        redirectChannelIfNeeded(intent)

        getTotalUnreadMessageCount(
            handler = UnreadMessageCountHandler { groupChannelCount: Int, feedChannelTotalCount: Int, e: SendbirdException? ->
                if (e != null) {
                    return@UnreadMessageCountHandler
                }
                Logger.i("updateChannelListTab [API] groupChannelCount=$groupChannelCount, feedChannelUnreadCount=$feedChannelTotalCount")
                drawUnreadCount(feedChannelListTab, feedChannelTotalCount)
            }
        )

        addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
            override fun onFriendsDiscovered(users: List<User>) {}
            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
                val groupChannelCount = unreadMessageCount.groupChannelCount
                val feedChannelUnreadCount = unreadMessageCount.feedChannelCount
                Logger.i("updateChannelListTab groupChannelCount=$groupChannelCount, feedChannelUnreadCount=$feedChannelUnreadCount")
                drawUnreadCount(feedChannelListTab, feedChannelUnreadCount)
            }
        })
    }

    @Synchronized
    private fun drawUnreadCount(tabView: CustomTabView, count: Int) {
        if (count > 0) {
            tabView.setBadgeVisibility(View.VISIBLE)
            tabView.setBadgeCount(if (count > 99) getString(R.string.text_tab_badge_max_count) else count.toString())
        } else {
            tabView.setBadgeVisibility(View.GONE)
        }
    }

    private fun redirectChannelIfNeeded(intent: Intent?) {
        if (intent == null) return
        Logger.i("++ intent: %s, %s", intent, intent.extras)
        if (intent.hasExtra(StringSet.PUSH_NOTIFICATION_DATA)) {
            intent.getSerializable(StringSet.PUSH_NOTIFICATION_DATA, HashMap::class.java)?.let {
                val resultMap = HashMap<String, String>()
                for ((key, value) in it) {
                    if (key is String && value is String) {
                        resultMap[key] = value
                    }
                }
                SendbirdPushHelper.markPushNotificationAsClicked(resultMap)
            }
            intent.removeExtra(StringSet.PUSH_NOTIFICATION_DATA)
        }
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
        }
        if (intent.hasExtra(StringSet.PUSH_REDIRECT_CHANNEL)) {
            val channelUrl = intent.getStringExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            if (channelUrl.isNullOrEmpty()) return

            binding.viewPager.currentItem = TAB_FEED_CHANNEL_LIST
            startActivity(FeedNotificationChannelActivity.newIntent(this, channelUrl))
        }
    }

    private class MainAdapter(val fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                TAB_FEED_CHANNEL_LIST -> FeedChannelListFragment()
                TAB_SETTINGS -> SampleSettingsFragment()
                else -> Fragment()
            }
        }

        fun getItem(position: Int): Fragment? {
            return fa.supportFragmentManager.findFragmentByTag("f$position")
        }
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
        private const val TAB_FEED_CHANNEL_LIST = 0
        private const val TAB_SETTINGS = 1
        fun newRedirectToChannelIntent(
            context: Context,
            channelUrl: String,
        ): Intent {
            return Intent(context, FeedChannelListMainActivity::class.java).apply {
                putExtra(StringSet.PUSH_REDIRECT_CHANNEL, channelUrl)
            }
        }
    }
}
