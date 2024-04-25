package com.sendbird.uikit.internal.singleton

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.params.MessageTemplateListParams
import com.sendbird.uikit.internal.model.templates.MessageTemplate
import com.sendbird.uikit.log.Logger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class is used to manage message templates related data which is used for [com.sendbird.android.channel.GroupChannel].
 * It doesn't manage the templates for Notification. For Notification, use [NotificationChannelManager].
 */
internal object MessageTemplateManager {
    internal lateinit var instance: MessageTemplateManagerImpl
    @VisibleForTesting
    internal val isInitialized: AtomicBoolean = AtomicBoolean()

    internal fun checkAndInit(context: Context) {
        if (!isInitialized.get()) {
            init(context)
        }
    }

    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        val messageTemplateRepository = MessageTemplateRepository(context.applicationContext)
        instance = MessageTemplateManagerImpl(messageTemplateRepository)
        isInitialized.set(true)
    }

    @JvmStatic
    fun hasTemplate(key: String?): Boolean {
        key ?: return false
        return instance.hasTemplate(key)
    }

    @JvmStatic
    fun getTemplate(key: String?): MessageTemplate? {
        key ?: return null
        return instance.getTemplate(key)
    }

    @WorkerThread
    @JvmStatic
    @Throws(SendbirdException::class)
    fun syncMessageTemplateListBlocking(latestToken: String?) = instance.syncMessageTemplateListBlocking(latestToken)

    @JvmStatic
    @Throws(SendbirdException::class)
    fun getMessageTemplatesBlocking(
        keys: List<String>
    ): List<MessageTemplate> = instance.getMessageTemplatesBlocking(keys)

    @JvmStatic
    fun clearAll() = instance.clearAll()

    @VisibleForTesting
    internal fun isInstanceInitialized() = this::instance.isInitialized
}

internal class MessageTemplateManagerImpl(private val messageTemplateRepository: MessageTemplateRepository) {
    private val worker = Executors.newSingleThreadExecutor()
    fun hasTemplate(key: String): Boolean = messageTemplateRepository.getTemplate(key) != null

    @WorkerThread
    @Throws(SendbirdException::class)
    fun syncMessageTemplateListBlocking(latestToken: String?) {
        if (latestToken == null) return // it means there's no template in the server.

        // 1. check updated time with server.
        val lastTemplateToken = messageTemplateRepository.lastCachedToken
        if (lastTemplateToken.isNotEmpty() && lastTemplateToken == latestToken) {
            Logger.d("++ skip request template list. The template list is already up-to-date.")
            return
        }

        // 2. call api
        messageTemplateRepository.requestMessageTemplatesBlocking()
    }

    @Throws(SendbirdException::class)
    @VisibleForTesting
    internal fun getMessageTemplatesBlocking(keys: List<String>): List<MessageTemplate> {
        Logger.d("MessageTemplateManager::getMessageTemplatesBlocking(keys: ${keys.joinToString()})")
        val cachedTemplates = mutableListOf<MessageTemplate>()
        val uncachedKeys = keys.filter {
            val template = messageTemplateRepository.getTemplate(it)
            if (template != null) cachedTemplates.add(template)
            template == null
        }

        Logger.d("MessageTemplateManager::getMessageTemplatesBlocking uncachedKeys: ${uncachedKeys.joinToString()}")
        if (uncachedKeys.isEmpty()) {
            return cachedTemplates
        }

        return cachedTemplates + messageTemplateRepository.requestMessageTemplatesBlocking(
            params = MessageTemplateListParams(limit = 100, keys = keys)
        )
    }

    fun getTemplate(key: String): MessageTemplate? {
        return messageTemplateRepository.getTemplate(key)
    }

    fun clearAll() {
        Logger.d("MessageTemplateManager::clearAll()")
        messageTemplateRepository.clearAll()
    }
}
