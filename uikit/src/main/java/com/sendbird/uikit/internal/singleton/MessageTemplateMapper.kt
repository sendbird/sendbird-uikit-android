package com.sendbird.uikit.internal.singleton

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.sendbird.android.message.BaseMessage
import com.sendbird.message.template.model.MessageTemplate
import com.sendbird.uikit.internal.extensions.messageTemplateStatus
import com.sendbird.uikit.internal.model.templates.MessageTemplateStatus
import com.sendbird.uikit.log.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal interface TemplateMapperDataProvider {
    fun isValid(message: BaseMessage): Boolean
    fun isTemplateMessage(message: BaseMessage): Boolean
    fun hasAllTemplates(message: BaseMessage): Boolean
    fun hasTemplate(key: String): Boolean
    fun getTemplateKey(message: BaseMessage): String?
    fun childTemplateKeys(message: BaseMessage): List<String>

    @WorkerThread
    fun requestTemplateListBlocking(keys: List<String>): List<MessageTemplate>
}

/**
 * Map [BaseMessage] and [MessageTemplate].
 */
internal class MessageTemplateMapper @JvmOverloads constructor(
    @get:VisibleForTesting
    internal val dataProvider: TemplateMapperDataProvider,
    private val worker: ExecutorService = Executors.newCachedThreadPool()
) {
    /**
     * Returns updated messages immediately.
     * Then, it requests un-cached MessageTemplates and will call onFetchCompleteHandler with fetched messages.
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
            val (templateMessages, notTemplateMessage) = mutableTemplateMessages.partition { dataProvider.isTemplateMessage(it) }
            templateMessages.forEach { it.messageTemplateStatus = MessageTemplateStatus.LOADING }
            notTemplateMessage.forEach { it.messageTemplateStatus = MessageTemplateStatus.NOT_APPLICABLE }

            Logger.d("2. filter template message result >> mutable[${mutableTemplateMessages.size}], template messages[${templateMessages.size}], not template messages[${notTemplateMessage.size}]")
            if (templateMessages.isEmpty()) {
                return mutableTemplateMessages
            }

            // 3. filter not cached template keys
            val (cachedTemplateMessages, notCachedTemplateMessages) = templateMessages.partition {
                dataProvider.hasAllTemplates(it)
            }

            cachedTemplateMessages.forEach {
                it.messageTemplateStatus = if (dataProvider.isValid(it)) {
                    MessageTemplateStatus.CACHED
                } else {
                    Logger.i("This template message is not supported. key=${dataProvider.getTemplateKey(it)}")
                    MessageTemplateStatus.NOT_APPLICABLE
                }
            }

            Logger.d("3. filter not cached template keys result >> template messages[${templateMessages.size}], cached[${cachedTemplateMessages.size}], not cached[${notCachedTemplateMessages.size}]")

            if (notCachedTemplateMessages.isEmpty()) {
                return mutableTemplateMessages
            }

            println(">> filter mutable template message status result >> total[${messages.size}], mutable[${mutableTemplateMessages.size}]")
            // 4. fetch not cached templates
            worker.submit {
                val parentTemplateKeys = notCachedTemplateMessages.mapNotNull {
                    dataProvider.getTemplateKey(it)
                }.filter { key -> dataProvider.hasTemplate(key).not() }

                val childTemplateKeys = notCachedTemplateMessages.map {
                    dataProvider.childTemplateKeys(it)
                }.flatten().filter { key -> dataProvider.hasTemplate(key).not() }

                val notCachedTemplateKeys = (parentTemplateKeys + childTemplateKeys).distinct()
                try {
                    println("notCachedTemplateKeys: ${notCachedTemplateKeys.size}")
                    Logger.d("notCachedTemplateKeys: ${notCachedTemplateKeys.size}")
                    if (notCachedTemplateKeys.isNotEmpty()) {
                        dataProvider.requestTemplateListBlocking(notCachedTemplateKeys)
                    }
                    val (fetchedMessages, notFetchedMessages) = notCachedTemplateMessages.partition { message ->
                        dataProvider.hasAllTemplates(message)
                    }
                    println("4. fetch not cached templates result >> fetched messages[${fetchedMessages.size}], not fetched messages[${notFetchedMessages.size}]")
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
