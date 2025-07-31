package com.sendbird.uikit.internal.extensions

import android.content.Context
import com.sendbird.android.annotation.AIChatBotExperimental
import com.sendbird.android.message.*
import com.sendbird.uikit.R
import com.sendbird.uikit.activities.adapter.MessageFormViewType
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.singleton.MessageDisplayDataManager
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.EmojiManager
import com.sendbird.uikit.model.MessageList
import com.sendbird.uikit.model.UserMessageDisplayData
import com.sendbird.uikit.utils.MessageUtils
import org.json.JSONObject

internal fun BaseMessage.hasParentMessage() = parentMessageId != 0L

internal fun BaseMessage.getDisplayMessage(): String {
    if (this.isTemplateMessage()) return StringSet.message
    return when (val data = MessageDisplayDataManager.getOrNull(this)) {
        is UserMessageDisplayData -> data.message ?: message
        else -> {
            message
        }
    }
}

internal fun MultipleFilesMessage.containsOnlyImageFiles(): Boolean {
    return files.all { it.fileType.contains(StringSet.image) }
}

internal fun MultipleFilesMessage.getCacheKey(index: Int): String = "${requestId}_$index"

internal fun BaseFileMessage.toDisplayText(context: Context): String {
    return when (this) {
        is FileMessage -> {
            if (MessageUtils.isVoiceMessage(this)) {
                context.getString(R.string.sb_text_voice_message)
            } else {
                this.type.toDisplayText(StringSet.file.upperFirstChar())
            }
        }

        is MultipleFilesMessage -> {
            StringSet.photo.upperFirstChar()
        }
    }
}

internal fun BaseFileMessage.getType(): String {
    return when (this) {
        is FileMessage -> {
            if (MessageUtils.isVoiceMessage(this)) {
                StringSet.voice
            } else {
                this.type
            }
        }

        is MultipleFilesMessage -> {
            this.files.firstOrNull()?.fileType ?: ""
        }
    }
}

internal fun BaseFileMessage.getName(context: Context): String {
    return when (this) {
        is FileMessage -> {
            if (MessageUtils.isVoiceMessage(this)) {
                context.getString(R.string.sb_text_voice_message)
            } else {
                this.name
            }
        }

        is MultipleFilesMessage -> {
            this.files.firstOrNull()?.fileName ?: ""
        }
    }
}

internal fun List<BaseMessage>.clearLastValidations() {
    this.flatMap { message -> message.messageForm?.items ?: emptyList() }
        .forEach { messageFormItem -> messageFormItem.shouldCheckValidation = null }
}

internal val lastValidations: MutableMap<String, Boolean?> = mutableMapOf()
internal var MessageFormItem.shouldCheckValidation: Boolean?
    get() = lastValidations["$id"]
    set(value) {
        if (value == null) {
            lastValidations.remove("$id")
        } else {
            lastValidations["$id"] = value
        }
    }
internal val MessageFormItem.isSubmittable: Boolean
    get() = (this.required == false && this.draftValues == null) || (!(this.draftValues.isNullOrEmpty()) && this.draftValues?.all { this.isValid(it) } == true)

internal fun MessageFormItem.MessageFormLayout.convertToViewType(): Int {
    return when (this) {
        MessageFormItem.MessageFormLayout.TEXT -> MessageFormViewType.TEXT.value
        MessageFormItem.MessageFormLayout.TEXTAREA -> MessageFormViewType.TEXTAREA.value
        MessageFormItem.MessageFormLayout.CHIP -> MessageFormViewType.CHIP.value
        else -> MessageFormViewType.UNKNOWN.value
    }
}

private val emojiCategoriesMap: MutableMap<Long, List<Long>> = mutableMapOf()
internal var BaseMessage.emojiCategories: List<Long>?
    get() = emojiCategoriesMap[this.messageId]
    set(value) {
        if (value == null) {
            emojiCategoriesMap.remove(this.messageId)
        } else {
            emojiCategoriesMap[this.messageId] = value
        }
    }

internal fun allowedEmojiList(message: BaseMessage): List<Emoji> {
    val categories = message.emojiCategories
    Logger.d("emoji categories for message: $categories")
    return if (categories == null) {
        EmojiManager.allEmojis
    } else {
        EmojiManager.getEmojis(categories) ?: emptyList()
    }
}

internal fun updateMessageEmojiCategories(messageList: List<BaseMessage>, emojiCategories: (BaseMessage) -> List<Long>?) {
    messageList.forEach { message ->
        if (message.reactions.isEmpty()) {
            // If there is no reaction, total emoji category allowed is not needed
            message.emojiCategories = null
            return@forEach
        }
        message.emojiCategories = emojiCategories(message)
    }
}

@OptIn(AIChatBotExperimental::class)
internal var BaseMessage.shouldShowSuggestedReplies: Boolean
    get() = this.extras[StringSet.should_show_suggested_replies] as? Boolean ?: false
    set(value) {
        this.extras[StringSet.should_show_suggested_replies] = value
    }

internal val BaseMessage.isStreamMessage: Boolean
    get() {
        val data = this.data
        if (data.isBlank()) {
            return false
        }
        return try {
            JSONObject(data).getBoolean(StringSet.stream)
        } catch (e: Exception) {
            false
        }
    }

internal val BaseMessage.disableChatInput: Boolean
    get() = extendedMessagePayload[StringSet.disable_chat_input] == true.toString()

internal fun MessageList.activeDisableInputMessageList(order: MessageList.Order): List<BaseMessage> {
    val copied = if (order == MessageList.Order.DESC) this.toList() else this.toList().asReversed()
    return copied.takeWhile { it.disableChatInput }
}

internal var newLineMessageId: Long? = null
internal val BaseMessage.isNewLineMessage: Boolean
    get() {
        return messageId == newLineMessageId
    }
