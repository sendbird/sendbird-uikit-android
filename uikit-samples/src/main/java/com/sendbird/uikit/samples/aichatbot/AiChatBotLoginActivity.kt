package com.sendbird.uikit.samples.aichatbot

import android.os.Bundle
import android.view.View
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.user.User
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.common.LoginActivity
import com.sendbird.uikit.samples.common.extensions.authenticate
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.utils.ContextUtils

class AiChatBotLoginActivity : LoginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            botIdLayout.visibility = View.VISIBLE
            botId.setText(PreferenceUtils.botId.ifEmpty { "client_bot" })
        }
    }

    override fun onSignUp() {
        Logger.i(">> AiChatBotLoginActivity::onSignUp(), userId=$inputUserId, nickname=$inputNickname")
        val botId = binding.botId.text.toString()
        if (botId.isEmpty()) return
        Logger.i("++ onSignUp botId=$botId")

        WaitingDialog.show(this)
        PreferenceUtils.botId = botId
        authenticate { _: User?, e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                PreferenceUtils.userId = ""
                PreferenceUtils.nickname = ""
                PreferenceUtils.botId = ""
                ContextUtils.toastError(this@AiChatBotLoginActivity, "${e.message}")
                return@authenticate
            }

            onSignIn()
        }
    }
}
