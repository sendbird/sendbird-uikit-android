package com.sendbird.uikit.samples.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.sendbird.uikit.activities.FeedNotificationChannelActivity
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.ThemeHomeActivity
import com.sendbird.uikit.samples.common.extensions.getFeedChannelUrl
import com.sendbird.uikit.samples.common.extensions.logout
import com.sendbird.uikit.samples.common.extensions.setTextColorResource
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.databinding.ActivityNotificationHomeBinding

class NotificationHomeActivity : ThemeHomeActivity() {
    override val binding: ActivityNotificationHomeBinding by lazy { ActivityNotificationHomeBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            chatAndFeedButton.visibility = if (PreferenceUtils.isUsingFeedChannelOnly) View.GONE else View.VISIBLE
            chatAndFeedButton.setOnClickListener {
                startActivity(Intent(this@NotificationHomeActivity, NotificationMainActivity::class.java))
            }
            feedOnlyButton.setOnClickListener {
                startActivity(
                    FeedNotificationChannelActivity.newIntent(
                        this@NotificationHomeActivity,
                        getFeedChannelUrl()
                    )
                )

            }
            btSignOut.setOnClickListener { logout() }
        }
    }

    override fun applyTheme() {
        super.applyTheme()
        binding.mainTitle.setTextColorResource(if (isDarkTheme) R.color.ondark_01 else R.color.onlight_01)
        binding.btSignOut.setBackgroundResource(
            if (isDarkTheme) R.drawable.selector_home_signout_button_dark
            else R.drawable.selector_home_signout_button
        )
    }
}
