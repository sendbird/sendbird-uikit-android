package com.sendbird.uikit.internal.extensions

import android.content.Context
import com.sendbird.android.annotation.AIChatBotExperimental
import com.sendbird.android.handler.CompletionHandler
import com.sendbird.android.message.BaseFileMessage
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.internal.model.Form
import com.sendbird.uikit.internal.singleton.JsonParser
import com.sendbird.uikit.internal.singleton.MessageDisplayDataManager
import com.sendbird.uikit.model.UserMessageDisplayData
import com.sendbird.uikit.utils.MessageUtils
import java.util.concurrent.ConcurrentHashMap

internal fun BaseMessage.hasParentMessage() = parentMessageId != 0L

internal fun BaseMessage.getDisplayMessage(): String {
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

@OptIn(AIChatBotExperimental::class)
internal fun BaseMessage.submitForm(form: Form, handler: CompletionHandler? = null) {
    val answers = form.formFields.fold(mutableMapOf<String, String>()) { acc, formField ->
        val answer = formField.temporaryAnswer ?: return@fold acc
        acc.apply { put(answer.formFieldKey, answer.answer) }
    }

    this.submitForm(form.formKey, answers) { e ->
        handler?.onResult(e)
    }
}

internal val BaseMessage.suggestedReplies: List<String>
    get() {
        val suggestedReplies = extendedMessagePayload[StringSet.suggested_replies] ?: return emptyList()
        return try {
            JsonParser.fromJson(suggestedReplies)
        } catch (e: Exception) {
            emptyList()
        }
    }

internal val formMap: MutableMap<Long, Pair<String, List<Form>>> = ConcurrentHashMap()

internal val BaseMessage.forms: List<Form>
    get() {
        formMap[this.messageId]?.let { return it.second }
        val forms = extendedMessagePayload[StringSet.forms] ?: return emptyList()
        return try {
            JsonParser.fromJson<List<Form>>(forms).onEach { form ->
                // setting answer to formField manually.
                val answeredList = form.answeredList
                form.formFields.forEach { formField ->
                    formField.messageId = this.messageId
                    formField.answer = answeredList?.find { it.formFieldKey == formField.formFieldKey }
                }
            }.also {
                formMap[messageId] = this.channelUrl to it
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

internal fun clearTemporaryAnswers(channelUrl: String) {
    formMap.forEach {
        if (it.value.first == channelUrl) {
            it.value.second.forEach { form ->
                form.formFields.forEach { formField ->
                    formField.temporaryAnswer = null
                    formField.lastValidation = null
                }
            }
        }
    }
}
