package com.sendbird.uikit.internal.extensions

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.ui.messages.MESSAGE_FORM_VERSION
import com.sendbird.uikit.model.configurations.ChannelConfig

internal fun GroupChannel.shouldDisableInput(channelConfig: ChannelConfig): Boolean {
    val disabledChatInputMessages = disabledChatInputMessagesMap[url]
    if (!disabledChatInputMessages.isNullOrEmpty()) {
        // Defensive code for handling cases where the 'Message after submission' is not sent by the server due to an error when the form is submitted.
        if (channelConfig.enableFormTypeMessage) {
            val messageForm = disabledChatInputMessages.find { it.messageForm != null }?.messageForm
            if (messageForm != null && messageForm.version <= MESSAGE_FORM_VERSION && !messageForm.isSubmitted) {
                return true
            }
        }

        // Defensive code for handling cases where 'Suggested reply' is being used and the server responds with 'disable_chat_input', but 'Suggested replies' are empty due to an error.
        if (channelConfig.enableSuggestedReplies) {
            val suggestedReplies = disabledChatInputMessages.find { it.suggestedReplies.isNotEmpty() }?.suggestedReplies
            if (!suggestedReplies.isNullOrEmpty()) {
                return true
            }
        }
        return false
    }
    return false
}

// DisabledChatInputMessage determines whether to block the chat input using `disabled_chat_input` when sending multiple consecutive messages in a workflow.
// In the future, it will be updated to handle `disabled_chat_input` through a Channel Event.
private val disabledChatInputMessagesMap: MutableMap<String, List<BaseMessage>> = mutableMapOf()

internal fun GroupChannel.saveDisabledChatInputMessages(messages: List<BaseMessage>) {
    disabledChatInputMessagesMap[url] = messages
}

internal fun GroupChannel.clearDisabledChatInputMessages() {
    disabledChatInputMessagesMap.remove(url)
}

internal val GroupChannel.containsBot: Boolean
    get() = this.hasBot || this.hasAiBot
