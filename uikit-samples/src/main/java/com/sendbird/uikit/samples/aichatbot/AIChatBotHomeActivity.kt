package com.sendbird.uikit.samples.aichatbot

import android.os.Bundle
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.ThemeHomeActivity
import com.sendbird.uikit.samples.common.extensions.logout
import com.sendbird.uikit.samples.common.extensions.setTextColorResource
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivityAiChatbotHomeBinding
import com.sendbird.uikit.utils.ContextUtils

class AIChatBotHomeActivity : ThemeHomeActivity() {
    override val binding by lazy { ActivityAiChatbotHomeBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.chatbotButton.setOnClickListener { startChatWithAiBot() }
        binding.btSignOut.setOnClickListener { logout() }
    }

    override fun applyTheme() {
        super.applyTheme()
        binding.mainTitle.setTextColorResource(if (isDarkTheme) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis)
        binding.btSignOut.setBackgroundResource(
            if (isDarkTheme) R.drawable.selector_home_signout_button_dark
            else R.drawable.selector_home_signout_button
        )
    }

    private fun startChatWithAiBot() {
        WaitingDialog.show(this)

        // Sendbird Connection must be made.
        SendbirdUIKit.connect { _, e ->
            WaitingDialog.dismiss()
            if (e != null) {
                ContextUtils.toastError(this, "Connection must be made. ${e.message}")
                return@connect
            }
            val botId = PreferenceUtils.botId
            SendbirdUIKit.startChatWithAiBot(this, botId, true) { error ->
                if (error != null) {
                    ContextUtils.toastError(this, "Failed to start chat with ai bot. ${error.message}")
                }
            }
        }
    }
}
