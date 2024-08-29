package com.sendbird.uikit.internal.extensions

import android.content.Context
import com.sendbird.android.annotation.AIChatBotExperimental
import com.sendbird.android.message.BaseFileMessage
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.FormField
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.android.shadow.com.google.gson.JsonParser
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.singleton.MessageDisplayDataManager
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.model.EmojiManager
import com.sendbird.uikit.model.UserMessageDisplayData
import com.sendbird.uikit.utils.MessageUtils

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
    this.flatMap { message -> message.forms }
        .flatMap { form -> form.formFields }
        .forEach { formField -> formField.lastValidation = null }
}

internal val lastValidations: MutableMap<String, Boolean?> = mutableMapOf()
internal var FormField.lastValidation: Boolean?
    get() = lastValidations[this.identifier]
    set(value) {
        if (value == null) {
            lastValidations.remove(this.identifier)
        } else {
            lastValidations[this.identifier] = value
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

private val FormField.identifier: String
    get() = "${this.messageId}_${this.key}"

@OptIn(AIChatBotExperimental::class)
internal var BaseMessage.shouldShowSuggestedReplies: Boolean
    get() = this.extras[StringSet.should_show_suggested_replies] as? Boolean ?: false
    set(value) {
        this.extras[StringSet.should_show_suggested_replies] = value
    }

@OptIn(AIChatBotExperimental::class)
internal var BaseMessage.isSuggestedRepliesVisible: Boolean
    get() = this.extras[StringSet.is_suggested_replies_visible] as? Boolean ?: false
    set(value) {
        this.extras[StringSet.is_suggested_replies_visible] = value
    }

internal val BaseMessage.isStreamMessage: Boolean
    get() {
        val data = this.data
        if (data.isBlank()) {
            return false
        }
        return try {
            JsonParser.parseString(data).asJsonObject[StringSet.stream].asBoolean
        } catch (e: Exception) {
            false
        }
    }
