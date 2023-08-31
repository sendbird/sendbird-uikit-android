package com.sendbird.uikit_messaging_android

import android.Manifest
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.widget.TextViewCompat
import com.sendbird.android.SendbirdChat.addUserEventHandler
import com.sendbird.android.SendbirdChat.getTotalUnreadMessageCount
import com.sendbird.android.SendbirdChat.removeUserEventHandler
import com.sendbird.android.SendbirdChat.sdkVersion
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.PushRequestCompleteHandler
import com.sendbird.android.handler.UnreadMessageCountHandler
import com.sendbird.android.handler.UserEventHandler
import com.sendbird.android.params.GroupChannelTotalUnreadMessageCountParams
import com.sendbird.android.user.UnreadMessageCount
import com.sendbird.android.user.User
import com.sendbird.uikit.BuildConfig
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.utils.ContextUtils
import com.sendbird.uikit_messaging_android.databinding.ActivityHomeBinding
import com.sendbird.uikit_messaging_android.groupchannel.GroupChannelMainActivity
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelMainActivity
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
import com.sendbird.uikit_messaging_android.utils.PushUtils
import com.sendbird.uikit_messaging_android.widgets.WaitingDialog
import java.util.Locale

/**
 * Displays a channel select screen.
 */
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean? -> }
    private val appSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _: ActivityResult? -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        val sdkVersion =
            String.format(resources.getString(R.string.text_version_info), BuildConfig.VERSION_NAME, sdkVersion)
        binding.tvVersionInfo.text = sdkVersion
        binding.groupChannelButton.setOnClickListener { clickGroupChannel() }
        binding.openChannelButton.setOnClickListener { clickOpenChannel() }
        binding.customSampleButton.setOnClickListener { moveToCustomSample() }
        binding.btSignOut.setOnClickListener { signOut() }
        TextViewCompat.setTextAppearance(binding.tvUnreadCount, R.style.SendbirdCaption3OnDark01)
        binding.tvUnreadCount.setBackgroundResource(R.drawable.shape_badge_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED) {
                return
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showPermissionRationalePopup()
                return
            }
            requestPermissionLauncher.launch(permission)
        }
    }

    override fun onResume() {
        super.onResume()
        // initialize total unread count
        getTotalUnreadMessageCount(
            GroupChannelTotalUnreadMessageCountParams(),
            UnreadMessageCountHandler { totalCount: Int, _: Int, e: SendbirdException? ->
                if (e != null) {
                    return@UnreadMessageCountHandler
                }
                if (totalCount > 0) {
                    binding.tvUnreadCount.visibility = View.VISIBLE
                    binding.tvUnreadCount.text =
                        if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString()
                } else {
                    binding.tvUnreadCount.visibility = View.GONE
                }
            })
        // register total unread count event
        addUserEventHandler(USER_EVENT_HANDLER_KEY, object : UserEventHandler() {
            override fun onFriendsDiscovered(users: List<User>) {}
            override fun onTotalUnreadMessageCountChanged(unreadMessageCount: UnreadMessageCount) {
                val totalCount = unreadMessageCount.groupChannelCount
                if (totalCount > 0) {
                    binding.tvUnreadCount.visibility = View.VISIBLE
                    binding.tvUnreadCount.text =
                        if (totalCount > 99) getString(R.string.text_tab_badge_max_count) else totalCount.toString()
                } else {
                    binding.tvUnreadCount.visibility = View.GONE
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        removeUserEventHandler(USER_EVENT_HANDLER_KEY)
    }

    private fun clickGroupChannel() {
        val intent = Intent(this, GroupChannelMainActivity::class.java)
        startActivity(intent)
    }

    private fun clickOpenChannel() {
        val intent = Intent(this, OpenChannelMainActivity::class.java)
        startActivity(intent)
    }

    private fun moveToCustomSample() {
        val url = "https://github.com/sendbird/sendbird-uikit-android/tree/main/uikit-custom-sample"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun signOut() {
        WaitingDialog.show(this)
        PushUtils.unregisterPushHandler(object : PushRequestCompleteHandler {
            override fun onComplete(isRegistered: Boolean, token: String?) {
                SendbirdUIKit.disconnect {
                    WaitingDialog.dismiss()
                    PreferenceUtils.clearAll()
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()
                }
            }

            override fun onError(e: SendbirdException) {
                WaitingDialog.dismiss()
            }
        })
    }

    private fun showPermissionRationalePopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(com.sendbird.uikit.R.string.sb_text_dialog_permission_title))
        builder.setMessage(
            String.format(
                Locale.US,
                getString(R.string.sb_text_need_to_allow_permission_notification),
                ContextUtils.getApplicationName(this)
            )
        )
        builder.setPositiveButton(com.sendbird.uikit.R.string.sb_text_go_to_settings) { _: DialogInterface?, _: Int ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            appSettingLauncher.launch(intent)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this, com.sendbird.uikit.R.color.secondary_300))
    }

    companion object {
        private val USER_EVENT_HANDLER_KEY = "USER_EVENT_HANDLER_KEY" + System.currentTimeMillis()
    }
}
