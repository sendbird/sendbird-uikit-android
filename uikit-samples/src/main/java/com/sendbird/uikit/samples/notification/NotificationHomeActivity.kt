package com.sendbird.uikit.samples.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.uikit.activities.FeedNotificationChannelActivity
import com.sendbird.uikit.samples.common.extensions.getFeedChannelUrl
import com.sendbird.uikit.samples.common.extensions.logout
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.databinding.ActivityNotificationHomeBinding

class NotificationHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityNotificationHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
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
}
