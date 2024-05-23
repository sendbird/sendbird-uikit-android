package com.sendbird.uikit.samples.aichatbot

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

class AiChatBotLoginActivity : LoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            title.visibility = View.GONE
            botIdLayout.visibility = View.VISIBLE
            notificationTitle.visibility = View.VISIBLE
            botId.setText(PreferenceUtils.botId.ifEmpty { "client_bot" })
        }
    }

    override fun onSignUp(userId: String, nickname: String) {
        Logger.i(">> AiChatBotLoginActivity::onSignUp(), userId=$userId, nickname=$nickname")
        val botId = binding.botId.text.toString()
        if (botId.isEmpty()) return
        Logger.i("++ onSignUp botId=$botId")

        WaitingDialog.show(this)
        PreferenceUtils.botId = botId
        authenticate { _: User?, e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(this@AiChatBotLoginActivity, "${e.message}")
                return@authenticate
            }
            PreferenceUtils.userId = userId
            PreferenceUtils.nickname = nickname
            SendbirdPushHelper.registerPushHandler(MyFirebaseMessagingService())
            val intent = PreferenceUtils.selectedSampleType.startingIntent(this)
            startActivity(intent)
            finish()
        }
    }
}
