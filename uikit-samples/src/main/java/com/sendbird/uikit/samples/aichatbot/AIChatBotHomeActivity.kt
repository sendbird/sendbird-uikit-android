package com.sendbird.uikit.samples.aichatbot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.samples.common.extensions.logout
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivityAiChatbotHomeBinding
import com.sendbird.uikit.utils.ContextUtils

class AIChatBotHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityAiChatbotHomeBinding.inflate(layoutInflater).apply {
            setContentView(root)
            chatbotButton.setOnClickListener { startChatWithAiBot() }
            btSignOut.setOnClickListener { logout() }
        }
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
            val botId = "client_bot"
            SendbirdUIKit.startChatWithAiBot(this, botId, true) { error ->
                if (error != null) {
                    ContextUtils.toastError(this, "Failed to start chat with ai bot. ${error.message}")
                }
            }
        }
    }
}
