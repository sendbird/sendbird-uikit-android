package com.sendbird.uikit.samples.notification

import android.os.Bundle
import android.view.View
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.android.user.User
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.common.LoginActivity
import com.sendbird.uikit.samples.common.extensions.authenticate
import com.sendbird.uikit.samples.common.extensions.startingIntent
import com.sendbird.uikit.samples.common.fcm.MyFirebaseMessagingService
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.utils.ContextUtils

class NotificationLoginActivity : LoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.useFeedOnly.visibility = View.VISIBLE
    }

    override fun onSignUp(userId: String, nickname: String) {
        Logger.i(">> NotificationLoginActivity::onSignUp(), userId=$userId, nickname=$nickname")
        WaitingDialog.show(this)
        PreferenceUtils.isUsingFeedChannelOnly = binding.useFeedOnly.isChecked
        authenticate { _: User?, e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(this@NotificationLoginActivity, "${e.message}")
                return@authenticate
            }
            PreferenceUtils.userId = userId
            PreferenceUtils.nickname = nickname
            SendbirdPushHelper.registerHandler(MyFirebaseMessagingService())
            val intent = PreferenceUtils.selectedSampleType.startingIntent(this)
            startActivity(intent)
            finish()
        }
    }
}
