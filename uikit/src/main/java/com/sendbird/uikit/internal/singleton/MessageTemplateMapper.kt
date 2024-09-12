package com.sendbird.uikit.internal.singleton

import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.extensions.childTemplateKeys
import com.sendbird.uikit.internal.extensions.isTemplateMessage
import com.sendbird.uikit.internal.extensions.isValid
import com.sendbird.uikit.internal.extensions.messageTemplateStatus
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Map [BaseMessage] and [com.sendbird.uikit.internal.model.templates.MessageTemplate].
 */
internal class MessageTemplateMapper(
    private val worker: ExecutorService = Executors.newCachedThreadPool()
) {
    /**
     * Returns updated messages immediately.
     * Then, it requests uncached MessageTemplates and will call onFetchCompleteHandler with fetched messages.
     */
    fun mapTemplate(messages: List<BaseMessage>, onFetchCompleteHandler: (updatedMessages: List<BaseMessage>) -> Unit): List<BaseMessage> {
        val startedAt = System.currentTimeMillis()
        try {
            // 1. filter mutable template message status
            val mutableTemplateMessages = messages.filter {
                it.messageTemplateStatus == null
            }

            Logger.d("1. filter mutable template message status result >> total[${messages.size}], mutable[${mutableTemplateMessages.size}]")
            if (mutableTemplateMessages.isEmpty()) {
                return mutableTemplateMessages
            }

            // 2. filter template message
            val (templateMessages, notTemplateMessage) = mutableTemplateMessages.partition { it.isTemplateMessage() }
            templateMessages.forEach { it.messageTemplateStatus = MessageTemplateStatus.LOADING }
            notTemplateMessage.forEach { it.messageTemplateStatus = MessageTemplateStatus.NOT_APPLICABLE }

            Logger.d("2. filter template message result >> mutable[${mutableTemplateMessages.size}], template messages[${templateMessages.size}], not template messages[${notTemplateMessage.size}]")
            if (templateMessages.isEmpty()) {
                return mutableTemplateMessages
            }

            // 3. filter not cached template keys
            val (cachedTemplateMessages, notCachedTemplateMessages) = templateMessages.partition {
                val templateMessageData = it.templateMessageData ?: return@partition false
                val hasParentTemplate = MessageTemplateManager.hasTemplate(templateMessageData.key)
                hasParentTemplate && templateMessageData.childTemplateKeys().all { key ->
                    MessageTemplateManager.hasTemplate(key)
                }
            }

            cachedTemplateMessages.forEach {
                it.messageTemplateStatus = if (it.templateMessageData.isValid()) {
                    MessageTemplateStatus.CACHED
                } else {
                    Logger.i("This template message is not supported. key=${it.templateMessageData}")
                    MessageTemplateStatus.NOT_APPLICABLE
                }
            }

            Logger.d("3. filter not cached template keys result >> template messages[${templateMessages.size}], cached[${cachedTemplateMessages.size}], not cached[${notCachedTemplateMessages.size}]")

            if (notCachedTemplateMessages.isEmpty()) {
                return mutableTemplateMessages
            }

            // 4. fetch not cached templates
            worker.submit {
                val parentTemplateKeys = notCachedTemplateMessages.mapNotNull {
                    it.templateMessageData?.key
                }.filter { key -> MessageTemplateManager.hasTemplate(key).not() }

                val childTemplateKeys = notCachedTemplateMessages.mapNotNull {
                    it.templateMessageData?.childTemplateKeys()
                }.flatten().filter { key -> MessageTemplateManager.hasTemplate(key).not() }

                val notCachedTemplateKeys = (parentTemplateKeys + childTemplateKeys).distinct()
                try {
                    MessageTemplateManager.getMessageTemplatesBlocking(notCachedTemplateKeys)
                    val (fetchedMessages, notFetchedMessages) = notCachedTemplateMessages.partition { message ->
                        val templateMessageData = message.templateMessageData ?: return@partition false
                        val hasParentTemplate = MessageTemplateManager.hasTemplate(templateMessageData.key)
                        hasParentTemplate && templateMessageData.childTemplateKeys().all { key ->
                            MessageTemplateManager.hasTemplate(key)
                        }
                    }
                    Logger.d("4. fetch not cached templates result >> fetched messages[${fetchedMessages.size}], not fetched messages[${notFetchedMessages.size}]")
                    fetchedMessages.forEach { it.messageTemplateStatus = MessageTemplateStatus.CACHED }
                    notFetchedMessages.forEach { it.messageTemplateStatus = MessageTemplateStatus.FAILED_TO_FETCH }
                } catch (e: Exception) {
                    Logger.d("4. fetch not cached templates result >> failed to fetch templates >> ${e.message}")
                    notCachedTemplateMessages.forEach { message ->
                        message.messageTemplateStatus = MessageTemplateStatus.FAILED_TO_FETCH
                    }
                }

                onFetchCompleteHandler(notCachedTemplateMessages)
            }

            return mutableTemplateMessages
        } finally {
            Logger.d("mapTemplate[size:${messages.size}] took ${System.currentTimeMillis() - startedAt}ms")
        }
    }
}
