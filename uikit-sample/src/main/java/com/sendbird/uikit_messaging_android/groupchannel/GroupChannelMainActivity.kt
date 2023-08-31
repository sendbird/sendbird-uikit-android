package com.sendbird.uikit_messaging_android.groupchannel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sendbird.android.SendbirdChat.addUserEventHandler
import com.sendbird.android.SendbirdChat.getTotalUnreadMessageCount
import com.sendbird.android.SendbirdChat.removeUserEventHandler
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.UnreadMessageCountHandler
import com.sendbird.android.handler.UserEventHandler
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams
import com.sendbird.android.user.UnreadMessageCount
import com.sendbird.android.user.User
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.ChannelActivity
import com.sendbird.uikit_messaging_android.R
import com.sendbird.uikit_messaging_android.SettingsFragment
import com.sendbird.uikit_messaging_android.consts.StringSet
import com.sendbird.uikit_messaging_android.databinding.ActivityGroupChannelMainBinding
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
import com.sendbird.uikit_messaging_android.widgets.CustomTabView

/**
 * Displays a group channel list screen.
 */
class GroupChannelMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChannelMainBinding
    private lateinit var unreadCountTab: CustomTabView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(SendbirdUIKit.getDefaultThemeMode().resId)
        binding = ActivityGroupChannelMainBinding.inflate(
            layoutInflater
        )
        val view: View = binding.root
        setContentView(view)
        initPage()
    }

    private fun initPage() {
        binding.vpMain.adapter = MainAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        val isDarkMode = PreferenceUtils.isUsingDarkTheme
        val backgroundRedId = if (isDarkMode) R.color.background_600 else R.color.background_50
        binding.tlMain.setBackgroundResource(backgroundRedId)
        binding.tlMain.setupWithViewPager(binding.vpMain)
        unreadCountTab = CustomTabView(this)
        unreadCountTab.setBadgeVisibility(View.GONE)
        unreadCountTab.setTitle(getString(R.string.text_tab_channels))
        unreadCountTab.setIcon(R.drawable.icon_chat_filled)
        val settingsTab = CustomTabView(this)
        settingsTab.setBadgeVisibility(View.GONE)
        settingsTab.setTitle(getString(R.string.text_tab_settings))
        settingsTab.setIcon(R.drawable.icon_settings_filled)
        binding.tlMain.getTabAt(0)?.customView = unreadCountTab
        binding.tlMain.getTabAt(1)?.customView = settingsTab
        redirectChannelIfNeeded(intent)
    }

    override fun onResume() {
        super.onResume()
        getTotalUnreadMessageCount(
            GroupChannelTotalUnreadMessageCountParams(),
            UnreadMessageCountHandler { totalCount: Int, _: Int, e: SendbirdException? ->
                if (e != null) {
                    return@UnreadMessageCountHandler
                }
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE)
                    unreadCountTab.setBadgeCount(if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString())
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE)
                }
            })
        addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
            override fun onFriendsDiscovered(users: List<User>) {}
            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
                val totalCount = unreadMessageCount.groupChannelCount
                if (totalCount > 0) {
                    unreadCountTab.setBadgeVisibility(View.VISIBLE)
                    unreadCountTab.setBadgeCount(if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString())
                } else {
                    unreadCountTab.setBadgeVisibility(View.GONE)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        removeUserEventHandler(USER_EVENT_HANDLER_KEY)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        redirectChannelIfNeeded(intent)
    }

    private fun redirectChannelIfNeeded(intent: Intent?) {
        if (intent == null) return
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
            intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
        }
        if (intent.hasExtra(StringSet.PUSH_REDIRECT_CHANNEL)) {
            val channelUrl = intent.getStringExtra(StringSet.PUSH_REDIRECT_CHANNEL)
                ?: return
            if (intent.hasExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)) {
                val messageId = intent.getLongExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, 0L)
                if (messageId > 0L) {
                    startActivity(ChannelActivity.newRedirectToMessageThreadIntent(this, channelUrl, messageId))
                    intent.removeExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID)
                }
            } else {
                startActivity(ChannelActivity.newIntent(this, channelUrl))
            }
            intent.removeExtra(StringSet.PUSH_REDIRECT_CHANNEL)
        }
    }

    private class MainAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {
        override fun getItem(position: Int): Fragment {
            return if (position == 0) {
                SendbirdUIKit.getFragmentFactory().newChannelListFragment(Bundle())
            } else {
                SettingsFragment()
            }
        }

        override fun getCount(): Int {
            return PAGE_SIZE
        }

        companion object {
            private const val PAGE_SIZE = 2
        }
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
        fun newRedirectToChannelIntent(
            context: Context,
            channelUrl: String,
            messageId: Long
        ): Intent {
            val intent = Intent(context, GroupChannelMainActivity::class.java)
            intent.putExtra(StringSet.PUSH_REDIRECT_CHANNEL, channelUrl)
            intent.putExtra(StringSet.PUSH_REDIRECT_MESSAGE_ID, messageId)
            return intent
        }
    }
}
