package com.sendbird.uikit.samples.customization.aichatbot

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.databinding.ActivityWebViewAiChatbotBinding
import com.sendbird.uikit.samples.databinding.DialogWebViewAiChatbotBinding

// Set the Bot ID
private const val BOT_ID = "client_bot"

fun showWebViewAiChatBotSample(activity: Activity) {
    activity.startActivity(Intent(activity, WebViewAiChatBotActivity::class.java))
}

class WebViewAiChatBotActivity : AppCompatActivity() {
    private val binding: ActivityWebViewAiChatbotBinding by lazy {
        ActivityWebViewAiChatbotBinding.inflate(layoutInflater)
    }
    private val aiChatBotDialogBinding: DialogWebViewAiChatbotBinding by lazy {
        DialogWebViewAiChatbotBinding.inflate(layoutInflater)
    }

    // Create the Dialog to chat with the AI ChatBot
    private val dialog: Dialog by lazy {
        Dialog(this).apply {
            setContentView(aiChatBotDialogBinding.root)
            window?.let {
                it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fbWidget.setOnClickListener {
            // Open the WebView to chat with the AI ChatBot
            showAIChatBotWidget()
        }
    }

    private fun showAIChatBotWidget() {
        // Load the WebView to chat with the AI ChatBot Widget
        with(aiChatBotDialogBinding.wvChatBot) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    // Handle error
                    Log.e("WebViewError", "Error: ${consoleMessage?.message()} at line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    if (consoleMessage?.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                        Toast.makeText(this@WebViewAiChatBotActivity, "An error occurred while loading the ChatBot. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            }

            if (url == null) {
                loadDataWithBaseURL(
                    "app://local", // Added baseUrl to preserve chat history when the page reloads, allowing restoration of previous chat sessions
                    widgetHtmlString(PreferenceUtils.appId, BOT_ID),
                    "text/html",
                    "utf-8",
                    null
                )
            }
        }
        dialog.show()
    }
}

private fun widgetHtmlString(appId: String, botId: String) =
    """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="ie=edge">
                <title>Chatbot</title>


                <!-- Load React first and then, ReactDOM. Also, these two libs' version should be same -->
                <script crossorigin src="https://unpkg.com/react@18.2.0/umd/react.development.js"></script>
                <script crossorigin src="https://unpkg.com/react-dom@18.2.0/umd/react-dom.development.js"></script>

                <!-- Load chat-ai-widget script and set process.env to prevent it get undefined -->
                <script>process = { env: { NODE_ENV: '' } }</script>
                <script crossorigin src="https://unpkg.com/@sendbird/chat-ai-widget@latest/dist/index.umd.js"></script>
                <link href="https://unpkg.com/@sendbird/chat-ai-widget@latest/dist/style.css" rel="stylesheet" />

                <!--Optional; to enable JSX syntax-->
                <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
                <style>
                    html,body { height:100% }
                    #aichatbot-widget-close-icon { display: none }
                </style>
              </head>
              <body>
                <!-- div element for chat-ai-widget container -->
                <div id="root"></div>

                <!-- Initialize chat-ai-widget and render the widget component -->
                <script type="text/babel">
                    const { ChatWindow } = window.ChatAiWidget
                    const App = () => {
                        return (
                            <ChatWindow
                                applicationId="$appId"
                                botId="$botId"
                            />
                        )
                    }
                    ReactDOM.createRoot(document.querySelector('#root')).render(<App/>);
                </script>
              </body>
            </html>
        """.trimIndent()
